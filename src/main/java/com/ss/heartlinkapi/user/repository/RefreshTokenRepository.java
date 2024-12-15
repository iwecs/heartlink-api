package com.ss.heartlinkapi.user.repository;



import org.springframework.data.repository.CrudRepository;

import com.ss.heartlinkapi.user.entity.RefreshToken;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String>{
	
    boolean existsByRefreshToken(String refreshToken);
}
