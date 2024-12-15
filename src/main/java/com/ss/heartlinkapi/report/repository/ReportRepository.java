package com.ss.heartlinkapi.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.ss.heartlinkapi.report.entity.ReportEntity;
import org.springframework.transaction.annotation.Transactional;

public interface ReportRepository extends JpaRepository<ReportEntity, Long>{

//    신고 반려처분하는 쿼리문
    @Transactional
    @Modifying
    @Query(value = "UPDATE report SET status = 'INVALID' WHERE id = :reportId", nativeQuery = true )
    void updateStatus(Long reportId);
}
