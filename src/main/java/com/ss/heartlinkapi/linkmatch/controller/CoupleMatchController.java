package com.ss.heartlinkapi.linkmatch.controller;

import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.linkmatch.dto.MatchAnswer;
import com.ss.heartlinkapi.linkmatch.dto.MatchAnswerListDTO;
import com.ss.heartlinkapi.linkmatch.service.CoupleMatchService;
import com.ss.heartlinkapi.linkmatch.entity.LinkMatchAnswerEntity;
import com.ss.heartlinkapi.linkmatch.entity.LinkMatchEntity;
import com.ss.heartlinkapi.linkmatch.service.CoupleMatchStatisticsService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/couple")
public class CoupleMatchController {

    @Autowired
    private CoupleMatchService coupleMatchService;

    @Autowired
    private CoupleService coupleService;

    @Autowired
    private CoupleMatchStatisticsService statisticsService;

    // 커플 매치 질문 조회
    @GetMapping("/missionmatch/questions")
    public ResponseEntity<?> getMatchQuestion() {
        // 오류 500 검사
        try {
            LinkMatchEntity result = coupleMatchService.getMatchQuestion();
            Map<String, Object> matchData = new HashMap<>();
            matchData.put("linkMatchId", result.getLinkMatchId());
            matchData.put("match1", result.getMatch1());
            matchData.put("match2", result.getMatch2());
            matchData.put("displayDate", result.getDisplayDate());
            // 오류 404 검사
            if (result != null) {
                return ResponseEntity.ok(matchData);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 커플 매치 답변 저장
    @PostMapping("/missionmatch/questions/choose")
    public ResponseEntity<?> matchChoose(@RequestBody MatchAnswer matchAnswer, @AuthenticationPrincipal CustomUserDetails user) {
        // 오류 500 검사
        try {
            // 오류 400 검사
            if (matchAnswer == null || matchAnswer.getQuestionId() == null
                    || matchAnswer.getSelectedOption() > 1 || matchAnswer.getSelectedOption() < 0 || user.getUserId() == null) {
                return ResponseEntity.badRequest().build();
            }
            LinkMatchAnswerEntity result = coupleMatchService.answerSave(matchAnswer, user.getUserEntity());
            // 오류 404 검사
            if (result != null) {
                // 매칭 성공 여부 확인
                int matchResult = coupleMatchService.checkTodayMatching(result.getCoupleId().getCoupleId());
                if(matchResult == 1) { // 매칭 성공
                    // 매칭 성공 시 커플엔티티 카운트 수 증가
                    int countUpresult = coupleService.matchCountUp(result.getCoupleId().getCoupleId());
                    if(countUpresult == 1) {
                        // 카운트 증가 성공하여 1 반환
                        return ResponseEntity.status(HttpStatus.OK).body(countUpresult);
                    } else {
                        // 카운트 증가 실패
                        return ResponseEntity.ok("매칭 성공했으나 카운트 증가 실패");
                    }
                } else if(matchResult == 0) { // 매칭 실패
                    // 매칭 실패하여 0 반환
                    return ResponseEntity.status(HttpStatus.OK).body(matchResult);
                } else {
                    // 상대방이 선택지를 고르지 않아 2 반환
                    return ResponseEntity.status(HttpStatus.OK).body(matchResult);
                }
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    // 매치 답변 내역 조회
    @GetMapping("/missionmatch/answerList")
    public ResponseEntity<?> getMatchAnswerList(@AuthenticationPrincipal CustomUserDetails userDetails) {
        // 오류 500 검사
        try{
            // 오류 400 검사
            if(userDetails == null) {
                return ResponseEntity.badRequest().build();
            }

        CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(userDetails.getUserId());

            Set<MatchAnswerListDTO> answerList = coupleMatchService.findAnswerListByCoupleId(couple, userDetails.getUserId());
        return ResponseEntity.ok().body(answerList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 오늘 선택한 내 매치 답변 조회
    @GetMapping("/checkMyAnswer")
    public ResponseEntity<?> checkMyAnswer(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try{
            if(userDetails == null) {
                return ResponseEntity.badRequest().build();
            }

            // result가 0이면 매치1번, 1이면 매치2번, 2이면 미답변
            int result = coupleMatchService.checkMyTodayAnswer(userDetails.getUserEntity());
            return ResponseEntity.ok().body(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 통계 - 일일 매치 통계 조회(일일 매치 답변 별 성별 비율 통계, 일일 매칭된 커플 퍼센트, 월별 매칭 횟수 조회)
    @GetMapping("/statistics/dailyMatch")
    public ResponseEntity<?> getStatisticsDailyMatchById(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try{
            Date todayDate = new Date();
            LocalDate today = todayDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            LinkMatchEntity todayMatch = statisticsService.findMatchByDate(today);

            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(userDetails.getUserId());

            if(todayMatch == null || couple == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> rateResult = statisticsService.matchRate(todayMatch, couple.getCoupleId());

            if(rateResult == null) {
                return ResponseEntity.ok(new HashMap<>());
            }

            return ResponseEntity.ok(rateResult);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

}