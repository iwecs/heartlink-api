package com.ss.heartlinkapi.linkmatch.repository;

import com.ss.heartlinkapi.linkmatch.entity.LinkMatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CoupleMatchRepository extends JpaRepository<LinkMatchEntity, Long> {

    // 오늘의 매치 질문 확인
    public LinkMatchEntity findByDisplayDate(LocalDate today);

    // 매치 질문이 있는지 확인
    public List<LinkMatchEntity> findAllByDisplayDate(LocalDate today);
}
