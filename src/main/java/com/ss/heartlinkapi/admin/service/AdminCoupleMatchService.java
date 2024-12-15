package com.ss.heartlinkapi.admin.service;

import com.ss.heartlinkapi.admin.dto.AdminMatchDTO;
import com.ss.heartlinkapi.admin.repository.AdminCoupleMatchRepository;
import com.ss.heartlinkapi.linkmatch.entity.LinkMatchEntity;
import com.ss.heartlinkapi.linkmatch.repository.CoupleMatchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCoupleMatchService {

    @Autowired
    private AdminCoupleMatchRepository coupleMatchRepository;
    @Autowired
    private CoupleMatchRepository matchRepository;

    // 매치 질문 목록 조회
    public Page<LinkMatchEntity> findAllByOrderByIdDesc(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return coupleMatchRepository.findAllByOrderByLinkMatchIdDesc(pageable);
    }

    // 매치 질문 등록
    public LinkMatchEntity addMatchQuestion(AdminMatchDTO questionText) {
        List<LinkMatchEntity> matchEntity = matchRepository.findAllByDisplayDate(questionText.getDisplayDate());
        if(matchEntity.isEmpty()) {
            LinkMatchEntity linkMatchEntity = new LinkMatchEntity();
            linkMatchEntity.setMatch1(questionText.getMatch1());
            linkMatchEntity.setMatch2(questionText.getMatch2());
            linkMatchEntity.setDisplayDate(questionText.getDisplayDate());
            LinkMatchEntity result = coupleMatchRepository.save(linkMatchEntity);
            return result;
        } else {
            return null;
        }
    }

    // 매치 질문 수정
    public LinkMatchEntity updateMatchQuestion(Long questionId, AdminMatchDTO questionText) {
        LinkMatchEntity matchEntity = matchRepository.findByDisplayDate(questionText.getDisplayDate());
        if(matchEntity==null||matchEntity.getLinkMatchId().equals(questionId)) {
            LinkMatchEntity matchQuestion = coupleMatchRepository.findById(questionId).orElse(null);
            if (matchQuestion != null) {
                matchQuestion.setMatch1(questionText.getMatch1());
                matchQuestion.setMatch2(questionText.getMatch2());
                matchQuestion.setDisplayDate(questionText.getDisplayDate());
                return coupleMatchRepository.save(matchQuestion);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // 매치 질문 삭제
    public void deleteMatchQuestion(Long questionId) {
        coupleMatchRepository.deleteById(questionId);
    }

    // 질문 아이디로 검색
    public LinkMatchEntity findById(Long questionId) {
        return coupleMatchRepository.findById(questionId).orElse(null);
    }
}
