package com.ss.heartlinkapi.block.repository;

import com.ss.heartlinkapi.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ss.heartlinkapi.block.entity.BlockEntity;
import com.ss.heartlinkapi.couple.entity.CoupleEntity;

import java.util.List;

public interface BlockRepository extends JpaRepository<BlockEntity, Long>{
	
    /********* 차단한 유저와 차단된 유저로 블락 엔티티 반환 **********/
    BlockEntity findByBlockedIdAndBlockerId(UserEntity blockedId, UserEntity blockerId);
    
    /********* 차단한 유저와 차단된 유저로 블락 엔티티 존재 확인 **********/
    boolean existsByBlockerIdAndBlockedId(UserEntity blockedId, UserEntity blockerId);
    
    // 차단한 유저와 차단된 커플이 일치하는 차단이 존재하는지 확인
    boolean existsByBlockerIdAndCoupleId(UserEntity blocker, CoupleEntity couple);
    
    /********* 유저엔티티로 블락엔티티리스트 페이징 처리 후 반환 **********/
    Page<BlockEntity> findByBlockerId(UserEntity blocker, Pageable pageable);

    /********* 차단한 커플글 게시글 접근 제한 **********/
	boolean existsByBlockedId_UserIdAndBlockerId_UserId(Long userId, Long currentUserId);
	@Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
	           "FROM BlockEntity b WHERE b.blockedId.userId = :userId AND b.coupleId.coupleId = :coupleId")
	boolean existsByBlockedId_UserIdAndCoupleId_CoupleId(@Param("userId") Long userId, @Param("coupleId") Long coupleId);

    // 커플아이디로 차단된 리스트 조회
    List<BlockEntity> findByCoupleId(CoupleEntity coupleId);
}