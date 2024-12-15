package com.ss.heartlinkapi.couple.controller;

import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/couple")
public class CoupleController {

    @Autowired
    private CoupleService coupleService;

    @Autowired
    private UserRepository userRepository;

    // 디데이 설정
    @PostMapping("/dday")
    public ResponseEntity<?> setAnniversaryDay(@AuthenticationPrincipal CustomUserDetails user, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate date) {
        // 오류 500 검사
        try{
            // 오류 400 검사
            if(date == null) {
                return ResponseEntity.badRequest().build();
            }

            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(user.getUserId());

            // 오류 404 검사
            if(couple == null) {
                return ResponseEntity.badRequest().body("찾을 수 없는 커플입니다.");
            }

            if(couple.getAnniversaryDate()!=null) {
                return ResponseEntity.badRequest().body("이미 기념일이 설정되었습니다.");
            }
            couple.setAnniversaryDate(date);
            CoupleEntity setCouple = coupleService.setAnniversary(couple);

            if(setCouple == null) {
                return ResponseEntity.badRequest().body("기념일이 설정 실패하였습니다.");
            }

            return ResponseEntity.ok().body("기념일이 설정되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 기념일 날짜 조회
    @GetMapping("/ddayDate")
    public ResponseEntity<?> getAnniversaryDay(@AuthenticationPrincipal CustomUserDetails user) {
        try {
            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(user.getUserId());
            if(couple == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok().body(couple.getAnniversaryDate());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // D-DAY 일수 조회
    @GetMapping("/dday")
    public ResponseEntity<?> getDday(@AuthenticationPrincipal CustomUserDetails user) {
        try {
            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(user.getUserId());
            if(couple == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(coupleService.getDday(couple));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 디데이 수정
    @PutMapping("/dday/update")
    public ResponseEntity<?> updateAnniversaryDay(@AuthenticationPrincipal CustomUserDetails user, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate date) {
        // 오류 500 검사
        try{
            // 오류 400 검사
            if(date == null) {
                return ResponseEntity.badRequest().build();
            }

            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(user.getUserId());
            // 오류 404 검사
            if(couple == null) {
                return ResponseEntity.notFound().build();
            }
            couple.setAnniversaryDate(date);
            CoupleEntity result = coupleService.setAnniversary(couple);

            if(result == null) {
                return ResponseEntity.badRequest().body("기념일 수정 실패하였습니다.");
            }
            return ResponseEntity.ok().body("기념일이 수정되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 자신의 커플 코드 확인
    @GetMapping("/match/code")
    public ResponseEntity<?> selectMyCode(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try{
            if(userDetails == null) {
                return ResponseEntity.badRequest().build();
            }

            UserEntity user = userRepository.findById(userDetails.getUserId()).orElse(null);

            if(user == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(user.getCoupleCode());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 커플 연결
    @PostMapping("/match/code/link")
    public ResponseEntity<?> coupleCodeMatch(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestParam String code){
        try{
            if(userDetails == null || code == null) {
                return ResponseEntity.badRequest().build();
            }
            UserEntity user = userRepository.findById(userDetails.getUserId()).orElse(null);

            // 유저 검색 실패
            if(user == null) {
                return ResponseEntity.notFound().build();
            }

            if(user.getCoupleCode().equals(code)) {
                // 자신의 코드를 입력했을 때
                return ResponseEntity.badRequest().body("자신의 커플 코드를 입력할 수 없습니다.");
            }

            // 존재하지 않는 커플코드인지 확인
            int checkResult = coupleService.codeCheck(code);
            if(checkResult==1) {
                return ResponseEntity.badRequest().body("존재하지 않는 커플코드입니다.");
            } else if(checkResult==2) {
                return ResponseEntity.badRequest().body("이미 커플 연결되어 있는 상대방입니다.");
            }

            CoupleEntity result = coupleService.coupleCodeMatch(user, code);

            if(result == null) {
                return ResponseEntity.badRequest().body("커플 연결에 실패하였습니다.");
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("커플 연결에 성공하였습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    //  커플 해지 (break_date 유예 3개월 설정)
    @PutMapping("/unlink")
    public ResponseEntity<?> unlinkCouple(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try{
            if(userDetails == null) {
                return ResponseEntity.badRequest().build();
            }

            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(userDetails.getUserId());

            if(couple == null) {
                return ResponseEntity.notFound().build();
            }

            if(couple.getBreakupDate() != null) {
                return ResponseEntity.badRequest().body("이미 연결 해지되어 유예 기간입니다. 연결 해지 예정일 : "+couple.getBreakupDate().plusDays(1));
            }
            CoupleEntity result = coupleService.setBreakDate(couple);

            if(result == null) {
                return ResponseEntity.badRequest().body("연결 해지 실패");
            }

            return ResponseEntity.ok("연결 해지가 완료되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 연결 해지 취소 (break_date 삭제)
    @PutMapping("/unlink/cancel")
    public ResponseEntity<?> cancelUnlinkCouple(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try{
            if(userDetails == null) {
                return ResponseEntity.badRequest().build();
            }

            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(userDetails.getUserId());

            if(couple == null) {
                return ResponseEntity.notFound().build();
            }

            if(couple.getBreakupDate()==null) {
                return ResponseEntity.badRequest().body("이미 연결되어 있는 상태입니다.");
            }

            CoupleEntity result = coupleService.deleteBreakDate(couple);
            if (result == null) {
                return ResponseEntity.badRequest().body("연결 해지 취소 실패");
            }
            return ResponseEntity.ok("연결 해지가 취소되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 유예기간 없이 즉시 연결 해지
    @DeleteMapping("/finalNowUnlink")
    public ResponseEntity<?> finalNowUnlinkCouple(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try{
            if(userDetails == null) {
                return ResponseEntity.badRequest().build();
            }

            CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(userDetails.getUserId());

            if(couple == null) {
                return ResponseEntity.notFound().build();
            }
            coupleService.finalNowUnlinkCouple(couple);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

    // 유예 상태인지 확인
    @GetMapping("/checkSoonBreak")
    public ResponseEntity<?> checkSoonBreak(@AuthenticationPrincipal CustomUserDetails userDetails) {
        try{
            if(userDetails == null) {
                return ResponseEntity.badRequest().body("해당되는 유저가 없습니다.");
            }

            UserEntity userEntity = userRepository.findById(userDetails.getUserEntity().getUserId()).orElse(null);
            if(userEntity.getRole().equals(Role.ROLE_SINGLE)){
                System.out.println(userDetails.getUserEntity().getUserId());
                System.out.println(userDetails.getUserEntity().getLoginId());
                System.out.println(userDetails.getUserEntity().getRole());
                return ResponseEntity.ok("true");
            } else {
                return ResponseEntity.ok("false");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }

    }

}
