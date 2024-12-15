package com.ss.heartlinkapi.user.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.UserEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long>{
	
	/*********** 전화번호로 유저 존재 확인 ***********/
	boolean existsByPhone(String phone);
	
	/*********** 로그인 아이디로 유저 존재 확인 ***********/
	boolean existsByLoginId(String loginId);
	
	/*********** 로그인 아이디로 유저 가져오기 ***********/
	UserEntity findByLoginId(String loginId);
	
	/*********** 전화번호로 유저 가져오기 ***********/
	UserEntity findByPhone(String phone);
	
	// 커플코드로 유저 검색
	UserEntity findByCoupleCode(String code);
	
	/*********** 유저 전체 검색 페이징 처리 ***********/
	Page<UserEntity> findAll(Pageable pageable);
	
	/*********** 유저 아이디 검색 페이징 처리 ***********/
	Page<UserEntity> findByUserId(Long userId, Pageable pageable);
	
	/*********** 유저 로그인 아이디 검색 페이징 처리 ***********/
	Page<UserEntity> findByLoginIdContaining(String loginId, Pageable pageable);
	
	/*********** 유저 가입일자 검색 페이징 처리 ***********/
	Page<UserEntity> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

	/*********** 유저 롤 검색 페이징 처리 ***********/
	Page<UserEntity> findByRole(Role role, Pageable pageable);

	// role이 single, couple 둘 중 해당되는 유저 리스트 반환
	List<UserEntity> findByRoleIn(List<Role> roles);

	/*********** 전화번호로 유저가 존재하고 비밀번호가 없는 회원인지 확인 ***********/
	boolean existsByPhoneAndPasswordIsNull(String phone);

	@Query("SELECT u FROM UserEntity u WHERE u.loginId LIKE CONCAT('%', :searchName, '%') ")
    List<UserEntity> findBySearchName(@Param("searchName") String searchName);
}
