package com.ss.heartlinkapi.oauth2.service;

import java.util.List;
import java.util.Random;

import com.ss.heartlinkapi.elasticSearch.service.ElasticService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ss.heartlinkapi.login.service.CoupleCode;
import com.ss.heartlinkapi.oauth2.dto.CustomOAuth2User;
import com.ss.heartlinkapi.oauth2.dto.GoogleResponse;
import com.ss.heartlinkapi.oauth2.dto.KakaoResponse;
import com.ss.heartlinkapi.oauth2.dto.NaverResponse;
import com.ss.heartlinkapi.oauth2.dto.OAuth2LoginDTO;
import com.ss.heartlinkapi.oauth2.dto.OAuth2Response;
import com.ss.heartlinkapi.user.entity.ProfileEntity;
import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.SocialEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.ProfileRepository;
import com.ss.heartlinkapi.user.repository.SocialRepository;
import com.ss.heartlinkapi.user.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final SocialRepository socialRepository;
	private final ProfileRepository profileRepository;
	private final PhoneService phoneService;
	private final LoginIdService loginIdService;
	private final ElasticService elasticService;

	public CustomOAuth2UserService(UserRepository userRepository, SocialRepository socialRepository,
			ProfileRepository profileRepository, PhoneService phoneService, LoginIdService loginIdService, ElasticService elasticService) {
		this.userRepository = userRepository;
		this.socialRepository = socialRepository;
		this.profileRepository = profileRepository;
		this.phoneService = phoneService;
		this.loginIdService = loginIdService;
		this.elasticService = elasticService;
	}

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);

		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		OAuth2Response oAuth2Response = null;

		if (registrationId.equals("naver")) {
			oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
		} else if (registrationId.equals("google")) {
			oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
		} else if (registrationId.equals("kakao")) {
			oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
		} else {
			return null;
		}

		String providerId = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();

		String phone = null;

		// OAuth2Response에서 전화번호 가져오기
		if (oAuth2Response.getPhone() != null) {
			phone = oAuth2Response.getProvider().equals("kakao") ? kakaoFormatPhone(oAuth2Response.getPhone())
					: oAuth2Response.getPhone();
		}
		
		// Redis에서 전화번호 가져오기
		String tempPhone = phoneService.retrieveTempPhone(providerId);
		if(tempPhone!=null) {
			phone = tempPhone;
		}

		// 전화번호가 여전히 null인 경우 처리
		if(phone==null) {
			if(socialRepository.existsByProviderId(providerId)) {
				UserEntity existingUser = socialRepository.findUserByProviderId(providerId);
				if(existingUser != null) {
					String loginId = existingUser.getLoginId();
					return login(oAuth2Response, loginId);
				}
			}
		    // 전화번호 입력 요청
		    OAuth2Error error = new OAuth2Error(providerId, "전화번호를 입력받아야 합니다.",null);
		    throw new OAuth2AuthenticationException(error);
	
		}

		UserEntity existData = userRepository.findByPhone(phone);
		
		if (existData == null) {
			// 회원가입
			UserEntity newUserEntity = null;
			try {
				newUserEntity = registerUser(oAuth2Response, phone, providerId);
			} catch (Exception e) {
				throw new OAuth2AuthenticationException(new OAuth2Error(providerId, e.getMessage(), null));
			}
			String loginId = newUserEntity.getLoginId();
			return login(oAuth2Response, loginId);
		} else {
			// 이미 있는 회원일때
			String loginId = existData.getLoginId();
			// 해당 소셜 계정의 존재 여부 확인
			String currentProvider = oAuth2Response.getProvider();
			List<SocialEntity> socialAccounts = existData.getSocialAccounts();
			boolean isLinked = socialAccounts.stream()
					.anyMatch(account -> account.getProvider().equals(currentProvider));
			if (!isLinked) {
				// 소셜 계정 추가
				SocialEntity socialEntity = new SocialEntity();
				socialEntity.setProvider(currentProvider);
				socialEntity.setProviderId(providerId);
				socialEntity.setUserEntity(existData);
				socialRepository.save(socialEntity);
			}
			return login(oAuth2Response, loginId);
		}

	}

	public CustomOAuth2User login(OAuth2Response oAuth2Response, String loginId) {
		
		OAuth2LoginDTO userDTO = new OAuth2LoginDTO();
		userDTO.setLoginId(loginId);
		userDTO.setName(oAuth2Response.getName());
		userDTO.setRole(Role.ROLE_USER);
		
		return new CustomOAuth2User(userDTO);
	}

	@Transactional
	public UserEntity registerUser(OAuth2Response oAuth2Response, String phone, String providerId) {
		
		// 유저 엔티티
		UserEntity userEntity = new UserEntity();
		userEntity.setName(oAuth2Response.getName());
		String gender = oAuth2Response.getGender();
		// 카카오 처리
		if (oAuth2Response.getProvider().equals("kakao")) {
			if (gender.equals("female")) {
				gender = "F";
			} else if (gender.equals("male")) {
				gender = "M";
			} else {
				gender = null;
			}
		}
		userEntity.setGender(gender == null ? 'U' : gender.charAt(0));
		userEntity.setPhone(phone);
		userEntity.setEmail(oAuth2Response.getEmail());
		userEntity.setCoupleCode(CoupleCode.generateRandomCode());
		userEntity.setRole(Role.ROLE_USER);

		String loginId = null;
		
		//Redis에서 아이디 가져오기
		loginId = loginIdService.retrieveTempPhone(providerId);
		
		// 아이디 입력 요청
		if(loginId==null) {
		    OAuth2Error error = new OAuth2Error(providerId, "아이디를 입력받아야 합니다.",null);
		    throw new OAuth2AuthenticationException(error);
		}
		userEntity.setLoginId(loginId);

		// 소셜 엔티티
		SocialEntity socialEntity = new SocialEntity();
		socialEntity.setProvider(oAuth2Response.getProvider());
		socialEntity.setProviderId(providerId);
		socialEntity.setUserEntity(userEntity);

		// 프로필 엔티티
		ProfileEntity profileEntity = new ProfileEntity();
		profileEntity.setNickname(generateNickname());
		profileEntity.setUserEntity(userEntity);

		// 디비에 값 저장
		userRepository.save(userEntity);
		elasticService.addUser(userEntity);
		socialRepository.save(socialEntity);
		profileRepository.save(profileEntity);
		
		return userEntity;
	}

	public String kakaoFormatPhone(String phone) {

		// 국내번호 그냥 저장, 외국번호 국가코드까지 저장
		if (phone.startsWith("+82")) {
			return "0" + phone.replaceAll("^\\+82 ", "").trim();
		} else {
			return phone.trim();
		}
	}

	public String generateNickname() {

		Random random = new Random();
		String[] prefixes = { "달콤한", "뽀짝", "사랑스런", "소중한", "귀여운", "반짝이는", "포근한", "상냥한", "활기찬", "장난꾸러기" };
		String[] suffixes = { "토끼", "강아지", "고양이", "햄스터", "다람쥐", "펭귄", "여우", "아기새", "고슴도치", "사슴" };

		String prefix = prefixes[random.nextInt(prefixes.length)];
		String suffix = suffixes[random.nextInt(suffixes.length)];

		return prefix + " " + suffix;
	}

}
