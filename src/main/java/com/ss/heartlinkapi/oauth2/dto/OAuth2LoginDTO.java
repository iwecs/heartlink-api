package com.ss.heartlinkapi.oauth2.dto;

import com.ss.heartlinkapi.user.entity.Role;

import lombok.Data;

@Data
public class OAuth2LoginDTO {

	private Role role;
	private String name;
	private String loginId;
}
