package com.ss.heartlinkapi.sms.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ss.heartlinkapi.login.service.CheckPhone;
import com.ss.heartlinkapi.sms.dto.VerificationDTO;
import com.ss.heartlinkapi.sms.service.SmsService;

import net.nurigo.sdk.message.response.SingleMessageSentResponse;

@RestController
@RequestMapping("/user/sms")
public class SmsController {
	
	private final SmsService smsService;
	
	public SmsController(SmsService smsService) {
		this.smsService = smsService;
	}


	@PostMapping("/send")
	public ResponseEntity<?> sendVerificationCode(@RequestParam String phoneNumber){
		
		// 전화번호 유효성 검사
		if(!CheckPhone.isPhoneValid(phoneNumber)) {
			 return ResponseEntity.badRequest().body("유효하지 않은 전화번호입니다.");
		}
		
		 try {
		        SingleMessageSentResponse response = smsService.sendVerificationCode(phoneNumber);

		        if (response != null && response.getMessageId() != null) {
		            return ResponseEntity.ok("인증번호가 전송되었습니다.");
		        } else {
		            return ResponseEntity.status(500).body("인증번호 전송에 실패했습니다.");
		        }
		        
		    } catch (Exception e) {
		        return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
		    }
		
	}
	
	@PostMapping("/validate")
	public ResponseEntity<?> validateCode(@RequestBody VerificationDTO verificationDTO){
		
		String phone = verificationDTO.getPhone();
		
		// 전화번호 유효성 검사
		if(!CheckPhone.isPhoneValid(phone)) {
			 return ResponseEntity.badRequest().body("유효하지 않은 전화번호입니다.");
		}
		
		String code = verificationDTO.getCode();
		
	    try {	    	
			boolean isValid = smsService.validateCode(phone,code);
			
	        if (isValid) {
	            return ResponseEntity.ok("인증번호가 확인되었습니다.");
	        } else {
	            return ResponseEntity.badRequest().body("인증번호가 일치하지 않습니다.");
	        }
	    } catch (Exception e) {
	    	return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
	    }
	}
	

}
