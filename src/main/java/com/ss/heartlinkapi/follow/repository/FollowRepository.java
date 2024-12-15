package com.ss.heartlinkapi.follow.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ss.heartlinkapi.follow.entity.FollowEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;

import java.util.List;

public interface FollowRepository extends JpaRepository<FollowEntity, Long>{
	
	/******* 유저가 팔로우하고 있는 팔로우엔티티 리스트 페이징 처리 후 반환 ******/
	Page<FollowEntity> findByFollowerUserId(Long userId, Pageable pageable);
	
	/******* 유저를 팔로우하고 있는 팔로우엔티티 리스트 페이징 처리 후 반환 ******/
	Page<FollowEntity> findByFollowingUserId(Long userId, Pageable pageable);
	
	/******* 팔로워유저와 팔로잉유저로 팔로우 객체 반환 ******/
    FollowEntity findByFollowerAndFollowing(UserEntity follower, UserEntity following);
    
	/******* 팔로워유저와 팔로잉유저로 팔로우 관계가 있는지 확인 ******/
    boolean existsByFollowerAndFollowing(UserEntity follower, UserEntity following);
    
	// 특정 사용자의 팔로잉 회원 수
	@Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.follower.id = :userId")
	int countFollowingByFollowerId(@Param("userId") Long userId);
	
	// 특정 사용자가 팔로우하는 회원 수
	@Query("SELECT COUNT(f) FROM FollowEntity f WHERE f.following.id = :userId")
	int countFollowersByUserId(@Param("userId") Long userId);

	// 사용자가 팔로잉하는 회원 리스트
	List<FollowEntity> findByFollowerUserIdAndStatusIsTrue(Long userId);

}
