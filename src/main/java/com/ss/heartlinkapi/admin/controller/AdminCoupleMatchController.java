package com.ss.heartlinkapi.admin.controller;

import com.ss.heartlinkapi.admin.dto.AdminMatchDTO;
import com.ss.heartlinkapi.admin.service.AdminCoupleMatchService;
import com.ss.heartlinkapi.linkmatch.entity.LinkMatchEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminCoupleMatchController {

    @Autowired
    private AdminCoupleMatchService adminCoupleMatchService;

    // 매치 질문 조회
    @GetMapping("/questions")
    public ResponseEntity<?> getAllquestions
    (@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        // 오류 500 검사
        try {
            Page<LinkMatchEntity> questions = adminCoupleMatchService.findAllByOrderByIdDesc(page, size);
            List<Map<String, Object>> questionsData = new ArrayList<>();
            for(LinkMatchEntity entity : questions) {
                Map<String, Object> questionData = new HashMap<>();
                questionData.put("questionId", entity.getLinkMatchId());
                questionData.put("match1", entity.getMatch1());
                questionData.put("match2", entity.getMatch2());
                questionData.put("displayDate", entity.getDisplayDate());
                questionsData.add(questionData);
            }
            return ResponseEntity.ok(questionsData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 매치 질문 등록
    @PostMapping("/questions")
    public ResponseEntity<?> addMatchQuestion(@RequestBody AdminMatchDTO questionText) {
        // 오류 500 검사
        try {
            // 오류 400 검사
            if (questionText == null || questionText.getMatch1() == null || questionText.getMatch2() == null
                    || questionText.getDisplayDate() == null) {
                return ResponseEntity.badRequest().build();
            }

            LinkMatchEntity result = adminCoupleMatchService.addMatchQuestion(questionText);
            if (result == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 매치 질문이 존재하는 날짜입니다.");
            }
            return ResponseEntity.status(HttpStatus.CREATED).body("매치 질문을 등록하였습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 매치 질문 수정
    @PutMapping("/questions/update")
    public ResponseEntity<?> updateMatchQuestion(@RequestParam(name = "questionId") Long questionId, @RequestBody AdminMatchDTO questionText) {
        // 오류 500 검사
        try{
            // 오류 400 검사
            if(questionId == null || questionText.getMatch1() == null || questionText.getMatch2() == null
                    || questionText.getDisplayDate() == null) {
                return ResponseEntity.badRequest().build();
            }
            LinkMatchEntity result = adminCoupleMatchService.updateMatchQuestion(questionId, questionText);
            // 오류 404 검사
            if (result == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("매치 질문 수정에 실패하였습니다.");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("매치 질문이 수정되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 매치 질문 삭제
    @DeleteMapping("/questions/{questionId}/delete")
    public ResponseEntity<?> deleteMatchQuestion(@PathVariable Long questionId) {
        // 오류 500 검사
        try{
            // 오류 400 검사
            if(questionId == null) {
                return ResponseEntity.badRequest().build();
            }

            LinkMatchEntity question = adminCoupleMatchService.findById(questionId);

            // 오류 404 검사
            if (question == null) {
                return ResponseEntity.notFound().build();
            } else {
                adminCoupleMatchService.deleteMatchQuestion(questionId);
                return ResponseEntity.ok().body("매치 질문이 삭제되었습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
