package com.ss.heartlinkapi.oauth2.service;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PhoneService {
	
	private final RedisTemplate<String, String> redisTemplate;
	
	public PhoneService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// 전화번호 임시 저장
	public void storeTempPhone(String providerId,String phone) {
		redisTemplate.opsForValue().set("auth:phone:" + providerId, phone,10,TimeUnit.MINUTES);
	}
	
	// 전화번호 꺼내오기
	public String retrieveTempPhone(String providerId) {
		return redisTemplate.opsForValue().get("auth:phone:" + providerId);
	}
	
}
