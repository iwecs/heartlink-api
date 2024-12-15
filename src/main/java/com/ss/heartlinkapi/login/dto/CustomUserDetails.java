package com.ss.heartlinkapi.login.dto;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.ss.heartlinkapi.user.entity.UserEntity;

public class CustomUserDetails implements UserDetails{
	
	private final UserEntity userEntity;

	public CustomUserDetails(UserEntity userEntity) {
		this.userEntity = userEntity;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singletonList(()->userEntity.getRole().name());
	}

	@Override
	public String getPassword() {
		return userEntity.getPassword();
	}

	@Override
	public String getUsername() {
		return userEntity.getLoginId();
	}
	
	public Long getUserId() {
		return userEntity.getUserId();
	}
	
	public UserEntity getUserEntity() {
		return userEntity;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true; // 계정 만료
	}

	@Override
	public boolean isAccountNonLocked() {
		return true; // 계정 장금
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true; // 비밀번호 만료
	}

	@Override
	public boolean isEnabled() {
		return true; // 계정 활성화
	}

}
