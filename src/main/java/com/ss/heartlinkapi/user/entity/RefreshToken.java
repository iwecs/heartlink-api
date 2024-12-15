package com.ss.heartlinkapi.user.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

@RedisHash(value = "refreshToken", timeToLive = 86400000L)
public class RefreshToken {
    @Id
    private String loginId;
    
    @Indexed
    private String refreshToken;

    public RefreshToken(String loginId, String refreshToken){
        this.loginId = loginId;
        this.refreshToken = refreshToken;
    }
}
