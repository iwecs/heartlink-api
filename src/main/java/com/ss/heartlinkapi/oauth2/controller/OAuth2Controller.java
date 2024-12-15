package com.ss.heartlinkapi.oauth2.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ss.heartlinkapi.oauth2.service.LoginIdService;
import com.ss.heartlinkapi.oauth2.service.PhoneService;


@RestController
@RequestMapping("/user/auth")
public class OAuth2Controller {
	
	private final PhoneService phoneService;
	private final LoginIdService loginIdService;
	
	public OAuth2Controller(PhoneService phoneService, LoginIdService loginIdService) {
		this.phoneService = phoneService;
		this.loginIdService = loginIdService;
	}

	@PostMapping("/phone")
	public ResponseEntity<Void> setPhoneNumber(@RequestBody Map<String, String> request,@RequestParam String providerId){
		String phone = request.get("phone");
		phoneService.storeTempPhone(providerId, phone);
		return ResponseEntity.ok().build();
	}
	
	@PostMapping("/loginId")
	public ResponseEntity<Void> setLoginId(@RequestBody Map<String, String> request,@RequestParam String providerId){
		String loginId = request.get("loginId");
		loginIdService.storeTempLoginId(providerId, loginId);
		return ResponseEntity.ok().build();
	}
	
}
