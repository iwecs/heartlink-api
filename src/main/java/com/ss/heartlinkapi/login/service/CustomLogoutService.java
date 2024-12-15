package com.ss.heartlinkapi.login.service;

import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.login.jwt.JWTUtil;
import com.ss.heartlinkapi.user.repository.RefreshTokenRepository;

import io.jsonwebtoken.ExpiredJwtException;


@Service
public class CustomLogoutService {
	
	private final JWTUtil jwtUtil;
	private final RefreshTokenRepository refreshTokenRepository;
	
	public CustomLogoutService(JWTUtil jwtUtil, RefreshTokenRepository refreshTokenRepository) {
		this.jwtUtil = jwtUtil;
		this.refreshTokenRepository = refreshTokenRepository;
	}
	
	public void logout(String refresh) {
		
		// 만료 여부 체크		
		if(jwtUtil.isExpired(refresh)) {
			throw new ExpiredJwtException(null, null, "이미 로그아웃되었습니다.");
		}
    	
    	// 토큰 카테고리 확인
		String category = jwtUtil.getCategory(refresh);
		if (!category.equals("refresh")) {
			throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
		}
		
		// DB 저장 여부 확인
		if(!refreshTokenRepository.existsByRefreshToken(refresh)) {
			throw new IllegalArgumentException("이미 로그아웃되었습니다.");
		}
		String loginId = jwtUtil.getLoginId(refresh);
		// DB 리프레쉬 토큰 삭제
		refreshTokenRepository.deleteById(loginId);

	}
	


}
