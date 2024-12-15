package com.ss.heartlinkapi.login.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinDTO {
	
	private String loginId;
	private String name;
	private String password;
	private String email;
	private char gender;
	private String nickname;
	private String phone;
	private String coupleCode;	
}
