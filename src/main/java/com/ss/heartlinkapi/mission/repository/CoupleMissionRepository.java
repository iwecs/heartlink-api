package com.ss.heartlinkapi.mission.repository;

import com.ss.heartlinkapi.mission.entity.LinkMissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoupleMissionRepository extends JpaRepository<LinkMissionEntity, Long> {

    // 매월 커플 미션 태그 리스트 조회
    @Query("select m from LinkMissionEntity m join m.linkTagId t where" +
            " function('YEAR', m.start_date) = :year " +
            "and function('MONTH', m.end_date) = :month " +
            "and function('YEAR', m.end_date) = :year " +
            "and function('MONTH', m.end_date) = :month ")
    List<LinkMissionEntity> findMissionByYearMonth(@Param("year") int year, @Param("month") int month);

}
