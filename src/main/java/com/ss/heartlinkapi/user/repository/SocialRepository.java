package com.ss.heartlinkapi.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ss.heartlinkapi.user.entity.SocialEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;

public interface SocialRepository extends JpaRepository<SocialEntity, Long> {
	
	/************ 프로바이더 아이디로 유저 존재 확인 *************/
	boolean existsByProviderId(String providerId);
	
	/************ 프로바이더 아이디로 유저엔티티 반환 *************/
	@Query("SELECT s.userEntity FROM SocialEntity s WHERE s.providerId = :providerId")
	UserEntity findUserByProviderId(@Param("providerId") String providerId);
	
	/************ 유저 엔티티로 프로바이더 엔티티 반환 *************/
	List<SocialEntity> findByUserEntity(UserEntity user);
	
	
}
