package com.ss.heartlinkapi.profile.service;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.follow.entity.FollowEntity;
import com.ss.heartlinkapi.follow.repository.FollowRepository;
import com.ss.heartlinkapi.login.service.CheckPassword;
import com.ss.heartlinkapi.login.service.LoginService;
import com.ss.heartlinkapi.profile.dto.ProfileDTO;
import com.ss.heartlinkapi.user.entity.ProfileEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.ProfileRepository;
import com.ss.heartlinkapi.user.repository.UserRepository;

@Service
public class ProfileService {
	
	private final ProfileRepository profileRepository;
	private final UserRepository userRepository;
	private final FollowRepository followRepository;
	private final CoupleService coupleService;
	private final LoginService loginService;

	public ProfileService(ProfileRepository profileRepository, UserRepository userRepository,
			FollowRepository followRepository, CoupleService coupleService, LoginService loginService) {
		this.profileRepository = profileRepository;
		this.userRepository = userRepository;
		this.followRepository = followRepository;
		this.coupleService = coupleService;
		this.loginService = loginService;
	}

	/******* 유저 아이디로 유저 엔티티 가져오는 메서드 *******/
	@Transactional(readOnly = true)
	public UserEntity findByUserId(Long userId) {
		return userRepository.findById(userId).orElse(null);
	}
	
    /******* 유저 프로필 정보 조회 메서드 *******/
	@Transactional(readOnly = true)
    public ProfileDTO getUserProfile(UserEntity userEntity, UserEntity loginUserEntity) {
    	
    	Long userId = userEntity.getUserId();
        ProfileEntity userProfile = selectProfile(userEntity);
        
        int followingCount = followRepository.countFollowingByFollowerId(userId);
        int followersCount = followRepository.countFollowersByUserId(userId);
        
        FollowEntity followEntity = followRepository.findByFollowerAndFollowing(loginUserEntity, userEntity);
        boolean isFollowed = followEntity != null;
        boolean followStatus = (followEntity != null && !followEntity.isStatus());
        
        CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(userId);
        Boolean isPrivate = couple.getIsPrivate();
        UserEntity coupleUserEntity = coupleService.getCouplePartner(userId);
        ProfileEntity coupleProfile = selectProfile(coupleUserEntity);
        
        return new ProfileDTO(
            userProfile.getProfile_img(),
            coupleProfile.getProfile_img(),
            userProfile.getBio(),
            userEntity.getLoginId(),
            userProfile.getNickname(),
            followersCount,
            followingCount,
            isFollowed,
            followStatus,
            coupleUserEntity.getUserId(),
            isPrivate
        );
    }
	
	/******* 유저로 프로필 엔티티 가져오는 메서드 *******/
	@Transactional(readOnly = true)
	public ProfileEntity selectProfile(UserEntity userEntity) {
		return profileRepository.findByUserEntity(userEntity);
	}
	
	/******* 유저로 프로필 가져오기 *******/
	@Transactional(readOnly = true)
	public ProfileEntity findByUserEntity(UserEntity user) {
		return profileRepository.findByUserEntity(user);
	}
	
	/******* 애칭 수정하기 *******/
    public void updateNickname(Long userId, String nickName) {
        UserEntity userEntity = findByUserId(userId);
        if (userEntity == null) {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }

        ProfileEntity profileEntity = findByUserEntity(userEntity);
        if (profileEntity == null) {
            throw new IllegalArgumentException("프로필이 존재하지 않습니다.");
        }

        if (nickName.length() < 1 || nickName.length() > 10) {
            throw new IllegalArgumentException("닉네임은 1자 이상 10자 이내여야 합니다.");
        }

        profileEntity.setNickname(nickName);
        profileRepository.save(profileEntity);
    }
    
	/******* 상태메세지 수정하기 *******/
    public void updateBio(Long userId, String bio) {
    	
        UserEntity userEntity = findByUserId(userId);
        
        if (userEntity == null) {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }

        if (bio.length() > 20) {
            throw new IllegalArgumentException("상태 메시지는 20자 이하로 입력해야 합니다.");
        }

        ProfileEntity profileEntity = findByUserEntity(userEntity);
        if (profileEntity == null) {
            throw new IllegalArgumentException("프로필이 존재하지 않습니다.");
        }

        profileEntity.setBio(bio);
        profileRepository.save(profileEntity);
    }
	/******* 비밀번호 변경하기 *******/
    public boolean updatePassword(Long userId, String beforePassword, String afterPassword) {
		UserEntity userEntity = findByUserId(userId);
		if (userEntity == null) {
			throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
		}
		
		boolean ismatch = loginService.checkPassword(userEntity, beforePassword);
		if (!ismatch) {
			throw new IllegalArgumentException("이전 비밀번호가 일치하지 않습니다.");
		}
		
		if (!CheckPassword.isPasswordValid(afterPassword)) {
			throw new IllegalArgumentException("비밀번호는 8~16자 이내이며, 특수문자, 영어, 숫자를 모두 포함해야 합니다.");
		}

		boolean isUpdated = loginService.updatePassword(userEntity, afterPassword);

    	return isUpdated;
    }
    
	/******* 프로필 이미지 수정 *******/
    public void updateProfileImage(Long userId, MultipartFile img) throws Exception {
    	
        UserEntity userEntity = findByUserId(userId);
        if (userEntity == null) {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }

        if (img.isEmpty()) {
            throw new IllegalArgumentException("이미지가 존재하지 않습니다.");
        }

        String originalFilename = img.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }

        // 확장자 확인
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!fileExtension.matches("\\.(jpg|jpeg|png)$")) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
        }

        // UUID로 파일 이름 변경
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        String currentPath = Paths.get("").toAbsolutePath().toString();
        String filePath = currentPath + "/images/" + newFileName;

        // 파일 생성
        img.transferTo(new File(filePath));

        // 프로필 업데이트
        ProfileEntity profileEntity = findByUserEntity(userEntity);
        if (profileEntity == null) {
            throw new IllegalArgumentException("프로필이 존재하지 않습니다.");
        }
        
        String imageUrl = "http://localhost:9090/images/" + newFileName;
        profileEntity.setProfile_img(imageUrl);
        profileRepository.save(profileEntity);
    }
    
	

}
