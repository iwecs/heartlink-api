package com.ss.heartlinkapi.mainpage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.mainpage.dto.UserCoupleDTO;
import com.ss.heartlinkapi.profile.service.ProfileService;
import com.ss.heartlinkapi.user.entity.ProfileEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;

@RestController
@RequestMapping("/user/couple")
public class MainPageController {
	
	private final CoupleService coupleService;
	private final ProfileService profileService;

	public MainPageController(CoupleService coupleService, ProfileService profileService) {
		this.coupleService = coupleService;
		this.profileService = profileService;
	}


	@GetMapping("")
	public ResponseEntity<?> getCoupleProfile(@AuthenticationPrincipal CustomUserDetails loginUser){
		
	    try {
	        Long loginUserId = loginUser.getUserId();
	        
	        UserEntity coupleUserEntity = coupleService.getCouplePartner(loginUserId);
	        if (coupleUserEntity == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("커플 사용자를 찾을 수 없습니다.");
	        }
	        
	        Long coupleUserId = coupleUserEntity.getUserId();
	        
	        ProfileEntity coupleProfileEntity = profileService.findByUserEntity(coupleUserEntity);
	        if (coupleProfileEntity == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("커플 프로필을 찾을 수 없습니다.");
	        }
	        
	        String coupleNickname = coupleProfileEntity.getNickname();
	        String coupleImg = coupleProfileEntity.getProfile_img();
	        String coupleBio = coupleProfileEntity.getBio();
	        
	        UserCoupleDTO userCoupleDTO = new UserCoupleDTO(coupleUserId, coupleImg, coupleNickname,coupleBio);
	        
	        return ResponseEntity.ok(userCoupleDTO);    
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류: " + e);
	    }
	}

	
}
