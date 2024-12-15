package com.ss.heartlinkapi.linkmatch.service;

import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.linkmatch.dto.MatchAnswer;
import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.linkmatch.dto.MatchAnswerListDTO;
import com.ss.heartlinkapi.linkmatch.repository.CoupleMatchAnswerRepository;
import com.ss.heartlinkapi.linkmatch.repository.CoupleMatchRepository;
import com.ss.heartlinkapi.linkmatch.entity.LinkMatchAnswerEntity;
import com.ss.heartlinkapi.linkmatch.entity.LinkMatchEntity;
import com.ss.heartlinkapi.mission.entity.LinkMissionEntity;
import com.ss.heartlinkapi.mission.repository.CoupleMissionRepository;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class CoupleMatchService {

    @Autowired
    private CoupleMatchAnswerRepository answerRepository;

    @Autowired
    private CoupleMatchRepository matchRepository;

    @Autowired
    private CoupleService coupleService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CoupleMatchAnswerRepository coupleMatchAnswerRepository;
    @Autowired
    private CoupleMissionRepository coupleMissionRepository;
    @Autowired
    private CoupleMatchRepository coupleMatchRepository;

    // 커플 답변 저장
    public LinkMatchAnswerEntity answerSave(MatchAnswer matchAnswer, UserEntity user) {

        LinkMatchEntity match = matchRepository.findById(matchAnswer.getQuestionId()).orElse(null);
        LinkMatchAnswerEntity isAnswer = coupleMatchAnswerRepository.findByUserIdAndCreatedAt(user, match.getDisplayDate());
        Date now = new Date();
        LocalDate today = now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if(isAnswer == null) {
            isAnswer = new LinkMatchAnswerEntity();
            isAnswer.setUserId(userRepository.findById(user.getUserId()).orElse(null));
            isAnswer.setMatchId(match);
            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(user.getUserId());
            isAnswer.setCoupleId(couple);
            isAnswer.setCreatedAt(today);
            isAnswer.setChoice(matchAnswer.getSelectedOption());
        } else {
            isAnswer.setChoice(matchAnswer.getSelectedOption());
            isAnswer.setCreatedAt(today);
        }

            LinkMatchAnswerEntity entity = answerRepository.save(isAnswer);
            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(user.getUserId());
            int result = answerRepository.checkTodayMatching(couple.getCoupleId());
            return entity;
    }

    // 오늘의 매치 질문 조회
    public LinkMatchEntity getMatchQuestion() {
        Date now = new Date();
        LocalDate today = now.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return matchRepository.findByDisplayDate(today);
    }

    // 매칭 성공 체크
    public int checkTodayMatching(Long coupleId) {
        return answerRepository.checkTodayMatching(coupleId);
    }

    // 매치 답변 내역 조회
    public Set<MatchAnswerListDTO> findAnswerListByCoupleId(CoupleEntity couple, Long userId) {
        // 상대방 유저엔티티 구하기
        UserEntity partner;
        UserEntity user;
        if(couple.getUser1().getUserId().equals(userId)) {
            partner = userRepository.findById(couple.getUser2().getUserId()).orElse(null);
            user = userRepository.findById(couple.getUser1().getUserId()).orElse(null);
        } else {
            partner = userRepository.findById(couple.getUser1().getUserId()).orElse(null);
            user = userRepository.findById(couple.getUser2().getUserId()).orElse(null);
        }

        List<LinkMatchAnswerEntity> coupleList = answerRepository.findByCoupleId(couple);
        List<MatchAnswerListDTO> answerList = new ArrayList<>();
        for(LinkMatchAnswerEntity answerEntity : coupleList) {
            MatchAnswerListDTO dto = new MatchAnswerListDTO();
            LinkMatchEntity matchQuestion = matchRepository.findById(answerEntity.getMatchId().getLinkMatchId()).orElse(null);
            dto.setMatch1(matchQuestion.getMatch1());
            dto.setMatch2(matchQuestion.getMatch2());
            dto.setDate(matchQuestion.getDisplayDate());
            LinkMatchAnswerEntity myAnswer = coupleMatchAnswerRepository.findByUserIdAndCreatedAt(user, matchQuestion.getDisplayDate());
            if(myAnswer != null) {
                dto.setMyChoice(myAnswer.getChoice());
            } else {
                dto.setMyChoice(-1);
            }
            LinkMatchAnswerEntity partnerAnswer = coupleMatchAnswerRepository.findByUserIdAndCreatedAt(partner, matchQuestion.getDisplayDate());
            if(partnerAnswer != null) {
                dto.setPartnerChoice(partnerAnswer.getChoice());
            } else {
                dto.setPartnerChoice(-1);
            }

            answerList.add(dto);
        }
        Set<MatchAnswerListDTO> set = new HashSet<>(answerList);

        return set;
    }

    // 오늘의 내 매치답변 조회
    public int checkMyTodayAnswer(UserEntity userEntity) {
        LinkMatchAnswerEntity result = coupleMatchAnswerRepository.findByUserIdAndCreatedAt(userEntity, LocalDate.now());
        if(result != null) {
            if(result.getChoice()==0){
                return 0; // 매치 0번 답했을 때
            } else if(result.getChoice()==1){
                return 1; // 매치 1번 답했을 때
            }
        }
            return 2; // 미답변 했을 때
    }
}
