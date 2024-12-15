package com.ss.heartlinkapi.oauth2.dto;

import org.springframework.lang.Nullable;

public interface OAuth2Response {

    String getProvider();

    String getProviderId();

    String getEmail();

    String getName();
    
    @Nullable
    String getGender();

    @Nullable
    String getPhone();
	
}
