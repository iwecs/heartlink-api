package com.ss.heartlinkapi.user.repository;

import com.ss.heartlinkapi.user.entity.UserEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ss.heartlinkapi.user.entity.ProfileEntity;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long>{

    ProfileEntity findByUserEntity(UserEntity user);
    
    // 여러 프로필 조회
    List<ProfileEntity> findAllByUserEntity(UserEntity user);

}
