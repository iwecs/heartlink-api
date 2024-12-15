package com.ss.heartlinkapi.login.service;

import org.springframework.stereotype.Service;

import com.ss.heartlinkapi.user.entity.RefreshToken;
import com.ss.heartlinkapi.user.repository.RefreshTokenRepository;

@Service
public class RefreshTokenService {


    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void saveRefreshToken(String loginId, String refreshToken) {
        if (loginId == null || refreshToken == null) {
            throw new IllegalArgumentException("loginId, refreshToken null 불가");
        }
        RefreshToken refreshEntity = new RefreshToken(loginId, refreshToken);
        refreshTokenRepository.save(refreshEntity);
    }

    public void deleteByLoginId(String loginId) {
        refreshTokenRepository.deleteById(loginId);
    }

}
