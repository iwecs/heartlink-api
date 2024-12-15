package com.ss.heartlinkapi.oauth2.dto;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomOAuth2User implements OAuth2User{
	
	private final OAuth2LoginDTO userDTO;

	public CustomOAuth2User(OAuth2LoginDTO userDTO) {
		this.userDTO = userDTO;
	}

	@Override
	public Map<String, Object> getAttributes() {

		return null;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		return Collections.singletonList(new SimpleGrantedAuthority(userDTO.getRole().name()));
	}

	@Override
	public String getName() {
		return userDTO.getName();
	}
	
	public String getLoginId() {
		return userDTO.getLoginId();
	}

}
