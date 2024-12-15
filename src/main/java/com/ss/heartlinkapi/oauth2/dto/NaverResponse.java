package com.ss.heartlinkapi.oauth2.dto;

import java.util.Map;

public class NaverResponse implements OAuth2Response{
	
	private final Map<String,Object> attribute;
	
	public NaverResponse(Map<String,Object> attribute) {
		this.attribute = (Map<String, Object>) attribute.get("response");
	}
	
	@Override
	public String getProvider() {
		
		return "naver";
	}

	@Override
	public String getProviderId() {
		
		return attribute.get("id").toString();
	}

	@Override
	public String getEmail() {
		
		return attribute.get("email").toString();
	}

	@Override
	public String getName() {
		
		return attribute.get("name").toString();
	}

	@Override
	public String getGender() {
		// F/M
		return attribute.get("gender").toString();
	}

	@Override
	public String getPhone() {
		// 010-0000-0000
		return attribute.get("mobile").toString();
	}

}
