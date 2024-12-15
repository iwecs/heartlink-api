package com.ss.heartlinkapi.login.service;

import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.login.jwt.JWTUtil;
import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.UserRepository;

@Service
public class JWTService {
	
	private final JWTUtil jwtUtil;
	private final UserRepository userRepository;

	public JWTService(JWTUtil jwtUtil, UserRepository userRepository) {
		super();
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
	}

	public CustomUserDetails validateToken(String token) throws Exception {
		
		// 만료 여부
		if (jwtUtil.isExpired(token)) {
			throw new RuntimeException("access token expired");
		}
		// 토큰 카테고리 확인
		String category = jwtUtil.getCategory(token);
		if (!category.equals("access")) {
			throw new RuntimeException("invalid access token");
		}
		String loginId = jwtUtil.getLoginId(token);
		String stringRole = jwtUtil.getRole(token);
		
		UserEntity userEntity = userRepository.findByLoginId(loginId);
		Role role = Role.valueOf(stringRole);
		userEntity.setRole(role);
		
		 return new CustomUserDetails(userEntity);
	}

}
