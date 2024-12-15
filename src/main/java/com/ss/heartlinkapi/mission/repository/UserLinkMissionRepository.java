package com.ss.heartlinkapi.mission.repository;

import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.mission.entity.UserLinkMissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLinkMissionRepository extends JpaRepository<UserLinkMissionEntity, Long> {

    // 커플 아이디로 해당되는 미션 달성 목록 받기
    public List<UserLinkMissionEntity> findAllByCoupleId(CoupleEntity couple);

    // 커플아이디로 삭제하기
    public void deleteAllByCoupleId(CoupleEntity couple);

    // 유저아이디로 완료된 커플 미션 조회
    
}