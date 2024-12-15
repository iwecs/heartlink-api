package com.ss.heartlinkapi.admin.repository;

import com.ss.heartlinkapi.linkmatch.entity.LinkMatchEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminCoupleMatchRepository extends JpaRepository<LinkMatchEntity, Long> {

    // 매치 질문 조회
    Page<LinkMatchEntity> findAllByOrderByLinkMatchIdDesc(Pageable pageable);

}
