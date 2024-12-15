package com.ss.heartlinkapi.oauth2.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response{
	
	private final Map<String,Object> userAttribute;
	private final Map<String,Object> attribute;
	
	public KakaoResponse(Map<String,Object> userAttribute) {
		this.userAttribute = userAttribute;
		this.attribute = (Map<String, Object>) userAttribute.get("kakao_account");
	}
	
	@Override
	public String getProvider() {

		return "kakao";
	}

	@Override
	public String getProviderId() {
		
		return userAttribute.get("id").toString();
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
		// female/male
		return attribute.get("gender").toString();
	}

	@Override
	public String getPhone() {
		// +82 10-0000-0000
		return attribute.get("phone_number").toString();
	}

}
