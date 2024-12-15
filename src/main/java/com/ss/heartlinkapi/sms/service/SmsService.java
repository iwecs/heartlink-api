package com.ss.heartlinkapi.sms.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.sms.util.SmsUtil;

import net.nurigo.sdk.message.response.SingleMessageSentResponse;

@Service
public class SmsService {
	
	private final SmsUtil smsUtil;
	private final RedisTemplate<String, String> redisTemplate;

	public SmsService(SmsUtil smsUtil, RedisTemplate<String, String> redisTemplate) {
		this.smsUtil = smsUtil;
		this.redisTemplate = redisTemplate;
	}

	// 코드 전송 후 redis 저장
	public SingleMessageSentResponse sendVerificationCode(String phone) {
		
		String formattedPhone = phone.replaceAll("-", "");
		String verificationCode = generateVerificationCode();
		
		redisTemplate.opsForValue().set("sms:phone:" + formattedPhone, verificationCode,5,TimeUnit.MINUTES);
		
		return smsUtil.sendOne(formattedPhone,verificationCode);
	}
	
	// 랜덤 코드 생성
	private String generateVerificationCode() {
		Random random = new Random();
		int code = 100000+random.nextInt(900000);		
		return String.valueOf(code);
	}

	// 코드 검증
	public boolean validateCode(String phone, String inputCode) {
		
		String formattedPhone = phone.replaceAll("-", "");
		// redis에서 값 꺼내오기
		String storedCode = redisTemplate.opsForValue().get("sms:phone:"+formattedPhone);
		
		return inputCode.equals(storedCode);
	}
	
	
	
	
}
