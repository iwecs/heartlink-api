package com.ss.heartlinkapi.oauth2.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginIdService {
	
	private final RedisTemplate<String, String> redisTemplate;

	public LoginIdService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}
	
	// 아이디 임시 저장
	public void storeTempLoginId(String providerId,String loginId) {
		redisTemplate.opsForValue().set("auth:loginId:"+providerId, loginId,10,TimeUnit.MINUTES);
	}
	
	// 아이디 꺼내오기
	public String retrieveTempPhone(String providerId) {
		return redisTemplate.opsForValue().get("auth:loginId:"+providerId);
	}
}
