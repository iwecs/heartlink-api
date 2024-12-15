package com.ss.heartlinkapi.admin.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.ss.heartlinkapi.user.entity.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserDTO {
	
	private Long userId;	// 유저 아이디
	private String loginId;	// 로그인 아이디
	private String email;	// 이메일
	private String name;	// 이름
	private char gender;	// 성별
	private String phone;	// 전화번호
	private Role role;	// 역할
	private LocalDateTime createdAt;	// 가입일
	private int dDay;	// 커플 디데이
	private Long coupleUserId;	// 커플 유저 아이디
	private int postCount;	// 작성한 글 수
	private int commentCount;	// 작성한 댓글 수
	private List<String> connectedSocails;	// 연결된 소셜 계정
	
}
