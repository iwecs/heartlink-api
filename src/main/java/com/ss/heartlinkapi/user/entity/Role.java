package com.ss.heartlinkapi.user.entity;

public enum Role {
	ROLE_USER, 		// 회원가입 완료 후 커플인증을 진행하지 않은 사용자
	ROLE_COUPLE, 	// 커플 인증을 완료한 사용자
	ROLE_SINGLE, 	// 커플 해지 후 유예기간에 있는 사용자
	ROLE_ADMIN 		// 관리자
}
