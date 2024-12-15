package com.ss.heartlinkapi.login.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.login.jwt.JWTUtil;
import com.ss.heartlinkapi.user.repository.RefreshTokenRepository;

@Service
public class ReissueService {
	private final JWTUtil jwtUtil;
	private final RefreshTokenService refreshTokenService;
	private final RefreshTokenRepository refreshTokenRepository;

	public ReissueService(JWTUtil jwtUtil, RefreshTokenService refreshTokenService,
			RefreshTokenRepository refreshTokenRepository) {
		this.jwtUtil = jwtUtil;
		this.refreshTokenService = refreshTokenService;
		this.refreshTokenRepository = refreshTokenRepository;
	}

	public ResponseEntity<Map<String, String>> reissueToken(String refreshToken) {

		// refreshToken이 없을 때
		if (refreshToken == null) {
			return new ResponseEntity<>(Map.of("error", "refresh token null"), HttpStatus.BAD_REQUEST);
		}

		// 만료 여부
		try {
			jwtUtil.isExpired(refreshToken);
		} catch (Exception e) {
			return new ResponseEntity<>(Map.of("error", "refresh token expired"), HttpStatus.UNAUTHORIZED);
		}

		// 토큰 카테고리 확인
		String category = jwtUtil.getCategory(refreshToken);

		if (!category.equals("refresh")) {
			return new ResponseEntity<>(Map.of("error", "invalid refresh token"), HttpStatus.UNAUTHORIZED);
		}
		
	    // 리프레시 토큰 존재 여부 확인
	    Boolean isExist = refreshTokenRepository.existsByRefreshToken(refreshToken);
	    
	    if (!isExist) {
	        return new ResponseEntity<>(Map.of("error", "invalid refresh token"), HttpStatus.BAD_REQUEST);
	    }
	    
		String loginId = jwtUtil.getLoginId(refreshToken);
		String role = jwtUtil.getRole(refreshToken);

		String newAccess = jwtUtil.createJwt("access", loginId, role, 600000L);
		String newRefresh = jwtUtil.createJwt("refresh", loginId, role, 86400000L);

        // 새로운 리프레시 토큰 저장
        refreshTokenService.saveRefreshToken(loginId, newRefresh);

		Map<String, String> tokens = new HashMap<>();
		tokens.put("accessToken", "Bearer " + newAccess);
		tokens.put("refreshToken", newRefresh);

		return ResponseEntity.ok(tokens);
	}

}
