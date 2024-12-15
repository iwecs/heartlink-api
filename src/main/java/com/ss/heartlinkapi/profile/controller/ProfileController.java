package com.ss.heartlinkapi.profile.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.profile.dto.ProfileDTO;
import com.ss.heartlinkapi.profile.dto.UpdatePasswordDTO;
import com.ss.heartlinkapi.profile.service.ProfileService;
import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.UserEntity;

@RestController
@RequestMapping("/user/profile")
public class ProfileController {

	private final ProfileService profileService;
	private final CoupleService coupleService;

	public ProfileController(ProfileService profileService, CoupleService coupleService) {
		this.profileService = profileService;
		this.coupleService = coupleService;
	}

	/***************** 로그인한 유저 아이디 반환 ******************/
	@GetMapping("")
	public ResponseEntity<?> selectProfile(@AuthenticationPrincipal CustomUserDetails loginUser) {

		if (loginUser == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유저 정보 없음");
		}

		try {
			Long loginUserId = loginUser.getUserId();
			return ResponseEntity.ok(loginUserId);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버오류 : " + e);
		}
	}

	/***************** 프로필 조회 ******************/
	@GetMapping("/{userId}")
	public ResponseEntity<?> selectProfile(@PathVariable Long userId, @AuthenticationPrincipal CustomUserDetails loginUser) {

		UserEntity userEntity = profileService.findByUserId(userId);
		if (userEntity == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
		}
		
		UserEntity loginUserEntity = loginUser.getUserEntity();
		if (loginUserEntity.getRole() == Role.ROLE_SINGLE && !loginUserEntity.getUserId().equals(userId)) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근 권한이 없습니다.");
	    }
		ProfileDTO profileDTO = profileService.getUserProfile(userEntity, loginUserEntity);
		
		return ResponseEntity.ok(profileDTO);
	}

	/***************** 내 프로필에서 비밀번호 변경 ******************/

	@PatchMapping("/{userId}/update/password")
	public ResponseEntity<?> updatePassword(@PathVariable Long userId, @RequestBody UpdatePasswordDTO updatePasswordDTO,
			@AuthenticationPrincipal CustomUserDetails loginUser) {

		String beforePassword = updatePasswordDTO.getBeforePassword();
		String afterPassword = updatePasswordDTO.getAfterPassword();

		if (!userId.equals(loginUser.getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
		}
	    try {
	        boolean isUpdated = profileService.updatePassword(userId, beforePassword, afterPassword);
	        if (isUpdated) {
	            return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
	        } else {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비밀번호 변경 실패");
	        }
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    }
	}

	/***************** 애칭 수정 ******************/
	@PatchMapping("/{userId}/update/nickname")
	public ResponseEntity<?> updateNickname(@PathVariable Long userId, @RequestBody Map<String, String> request,
			@AuthenticationPrincipal CustomUserDetails loginUser) {
		
	    String nickName = request.get("nickName");

	    if (!userId.equals(loginUser.getUserId())) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
	    }

	    try {
	        profileService.updateNickname(userId, nickName);
	        return ResponseEntity.ok("애칭이 성공적으로 변경되었습니다.");
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    }

	}

	/***************** 상태메시지 수정 ******************/
	@PatchMapping("/{userId}/update/bio")
	public ResponseEntity<?> updateProfileBio(@PathVariable Long userId, @RequestBody Map<String, String> request,
			@AuthenticationPrincipal CustomUserDetails loginUser) {

		String bio = request.get("bio");

		if (!userId.equals(loginUser.getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
		}

	    try {
	        profileService.updateBio(userId, bio);
	        return ResponseEntity.ok("상태메시지가 수정되었습니다.");
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    }
	}

	/***************** 프로필 이미지 수정 ******************/
	@PatchMapping("/{userId}/update/img")
	public ResponseEntity<?> updateProfileImage(@PathVariable Long userId, @RequestParam("img") MultipartFile img,
	        @AuthenticationPrincipal CustomUserDetails loginUser) {

	    if (!userId.equals(loginUser.getUserId())) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
	    }

	    try {
	        profileService.updateProfileImage(userId, img);
	        return ResponseEntity.ok("프로필 이미지가 성공적으로 업데이트되었습니다.");
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이미지 저장 오류 : " + e.getMessage());
	    }
	}

	/***************** 팔로워만 공개 설정 ******************/

	@PatchMapping("/{userId}/update/private")
	public ResponseEntity<?> updatePrivate(@PathVariable Long userId,
			@AuthenticationPrincipal CustomUserDetails loginUser) {

		UserEntity userEntity = profileService.findByUserId(userId);
		if (userEntity == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
		}

		if (!userId.equals(loginUser.getUserId())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
		}

		CoupleEntity coupleEntity = coupleService.findCoupleEntity(userEntity);

		if (coupleEntity == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("커플 정보를 찾을 수 없습니다.");
		}

		if (coupleEntity.getIsPrivate()) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 비공개 설정이 되어 있습니다.");
		}

		coupleService.updateIsPrivate(coupleEntity);

		return ResponseEntity.ok("커플 비공개 설정이 완료되었습니다.");

	}
	
	/***************** 전체 공개 설정 ******************/
	@PatchMapping("/{userId}/update/public")
	public ResponseEntity<?> updatePublic(@PathVariable Long userId,
	        @AuthenticationPrincipal CustomUserDetails loginUser) {

	    UserEntity userEntity = profileService.findByUserId(userId);
	    if (userEntity == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("유저를 찾을 수 없습니다.");
	    }

	    if (!userId.equals(loginUser.getUserId())) {
	        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("수정 권한이 없습니다.");
	    }

	    CoupleEntity coupleEntity = coupleService.findCoupleEntity(userEntity);

	    if (coupleEntity == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("커플 정보를 찾을 수 없습니다.");
	    }

	    if (!coupleEntity.getIsPrivate()) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 전체 공개 설정이 되어 있습니다.");
	    }
	    
	    coupleService.updatePublic(coupleEntity);
	    
	    return ResponseEntity.ok("커플 전체 공개 설정이 완료되었습니다.");
	}
	
	
}
