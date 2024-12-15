package com.ss.heartlinkapi.couple.service;

import com.ss.heartlinkapi.block.entity.BlockEntity;
import com.ss.heartlinkapi.block.repository.BlockRepository;
import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.couple.repository.CoupleRepository;
import com.ss.heartlinkapi.linkmatch.entity.LinkMatchAnswerEntity;
import com.ss.heartlinkapi.linkmatch.repository.CoupleMatchAnswerRepository;
import com.ss.heartlinkapi.mission.entity.UserLinkMissionEntity;
import com.ss.heartlinkapi.mission.service.CoupleMissionService;
import com.ss.heartlinkapi.post.service.PostService;
import com.ss.heartlinkapi.user.entity.Role;
import com.ss.heartlinkapi.user.entity.UserEntity;
import com.ss.heartlinkapi.user.repository.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CoupleService {
    @Autowired
    private CoupleRepository coupleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CoupleMatchAnswerRepository coupleMatchAnswerRepository;

    @Autowired
    @Lazy
    private CoupleMissionService coupleMissionService;

    @Autowired
    @Lazy
    private PostService postService;

    @Autowired
    private BlockRepository blockRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // 유저아이디로 커플객체 조회
    public CoupleEntity findByUser1_IdOrUser2_Id(Long id) {
        return coupleRepository.findCoupleByUserId(id);
    }

    // 유저객체로 커플객체 조회
    public CoupleEntity findCoupleEntity(UserEntity user) {
        return coupleRepository.findCoupleByUserId(user.getUserId());
    }

    // 커플 아이디로 커플 객체 반환
    public CoupleEntity findById(Long coupleId) {
        return coupleRepository.findById(coupleId).orElse(null);
    }

    // 디데이 날짜 추가/수정
    public CoupleEntity setAnniversary(CoupleEntity couple) {
        return coupleRepository.save(couple);
    }

    // 커플 매칭 카운트 증가
    public int matchCountUp(Long coupleId) {
        return coupleRepository.matchCountUp(coupleId);
    }

    // 커플 연결
    @Transactional
    public CoupleEntity coupleCodeMatch(UserEntity user1, String code) {
        code = code.toUpperCase().trim();
        UserEntity user2 = userRepository.findByCoupleCode(code);
        user1.setRole(Role.ROLE_COUPLE);
        user2.setRole(Role.ROLE_COUPLE);
        userRepository.save(user1);
        userRepository.save(user2);
        CoupleEntity newCouple = new CoupleEntity();
        newCouple.setUser1(user1);
        newCouple.setUser2(user2);
        newCouple.setCreatedAt(new Timestamp(new Date().getTime()));
        return coupleRepository.save(newCouple);
    }

    // 커플코드 연결 전 확인
    public int codeCheck(String code){
        code = code.toUpperCase().trim();
        UserEntity user = userRepository.findByCoupleCode(code);
        if(user == null) {
            return 1; // 존재하지 않는 커플코드
        }
        CoupleEntity couple = coupleRepository.findCoupleByUserId(user.getUserId());
        if(couple != null) {
            return 2; // 이미 상대가 커플임
        }
        return 3; // 정상
    }

    // 커플 해지일 설정
    public CoupleEntity setBreakDate(CoupleEntity couple) {
        LocalDate breakDate = LocalDate.now().plusMonths(3);
        couple.setBreakupDate(breakDate);
        couple.getUser1().setRole(Role.ROLE_SINGLE);
        couple.getUser2().setRole(Role.ROLE_SINGLE);
        userRepository.save(couple.getUser1());
        userRepository.save(couple.getUser2());
        return coupleRepository.save(couple);
    }

    // 커플 해지일 삭제
    public CoupleEntity deleteBreakDate(CoupleEntity couple) {
        couple.setBreakupDate(null);
        couple.getUser1().setRole(Role.ROLE_COUPLE);
        couple.getUser2().setRole(Role.ROLE_COUPLE);
        userRepository.save(couple.getUser1());
        userRepository.save(couple.getUser2());
        return coupleRepository.save(couple);
    }

    // 커플 유예기간 매일 체크 후 삭제 기능 (배치 프로그램)
    @Transactional
    public void batchFinalUnlinkCouple() {
        List<CoupleEntity> breakCouple = coupleRepository.findCoupleEntityByBreakupDateIsNotNull();
        if(breakCouple != null && breakCouple.size() > 0) {
            LocalDate today = LocalDate.now();
            for(CoupleEntity couple : breakCouple) {
                if(couple.getBreakupDate().isBefore(today)){
                    // 매치 답변 목록 삭제
                    List<LinkMatchAnswerEntity> answerList = coupleMatchAnswerRepository.findByCoupleId(couple);
                    if(answerList != null && answerList.size() > 0) {
                        coupleMatchAnswerRepository.deleteAllByCoupleId(couple);
                    }
                    // 차단 목록 삭제
                    List<BlockEntity> blockList = blockRepository.findByCoupleId(couple);
                    for(BlockEntity block : blockList) {
                        blockRepository.delete(block);
                        System.out.println("차단 목록에서 커플 삭제");
                    }
                    // 매치 미션 목록 삭제
                    List<UserLinkMissionEntity> userMissionList = coupleMissionService.findUserLinkMissionByCoupleId(couple);
                    if(userMissionList != null && userMissionList.size() > 0) {
                        coupleMissionService.deleteUserMissionByCoupleId(couple);
                    }
                    try {
                        couple.getUser1().setRole(Role.ROLE_USER);
                        couple.getUser2().setRole(Role.ROLE_USER);
                        couple.getUser1().setCoupleCode(generateRandomCode());
                        couple.getUser2().setCoupleCode(generateRandomCode());
                        userRepository.save(couple.getUser1());
                        userRepository.save(couple.getUser2());
                        coupleRepository.deleteById(couple.getCoupleId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    // 커플 코드 랜덤값 적용
    private final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final int CODE_LENGTH = 6;

    public String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    // 커플 해지 유예기간 없이 즉시 해지
    @Transactional
    public void finalNowUnlinkCouple(CoupleEntity couple) {
        UserEntity user1 = userRepository.findById(couple.getUser1().getUserId()).orElse(null);
        UserEntity user2 = userRepository.findById(couple.getUser2().getUserId()).orElse(null);
        List<LinkMatchAnswerEntity> answerList = coupleMatchAnswerRepository.findByCoupleId(couple);
        if(answerList != null && answerList.size() > 0) {
            coupleMatchAnswerRepository.deleteAllByCoupleId(couple);
        }

        // 매치 미션 목록 삭제
        List<UserLinkMissionEntity> userMissionList = coupleMissionService.findUserLinkMissionByCoupleId(couple);
        if(userMissionList != null && userMissionList.size() > 0) {
            coupleMissionService.deleteUserMissionByCoupleId(couple);
        }

        // 차단 목록 삭제
        List<BlockEntity> blockList = blockRepository.findByCoupleId(couple);
        for(BlockEntity block : blockList) {
            blockRepository.delete(block);
        }

        try {
            user2.setRole(Role.ROLE_USER);
            user1.setRole(Role.ROLE_USER);
            user1.setCoupleCode(generateRandomCode());
            user2.setCoupleCode(generateRandomCode());

            UserEntity result = userRepository.save(user1);
            UserEntity result2 = userRepository.save(user2);
            coupleRepository.deleteById(couple.getCoupleId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // 내 커플의 아이디 가져오기
    public UserEntity getCouplePartner(Long userId) {
    	CoupleEntity couple = coupleRepository.findCoupleByUserId(userId);
    	if(couple != null) {
    		if(couple.getUser1().getUserId().equals(userId)) {
    			return couple.getUser2();
    		} else {
    			return couple.getUser1();
    		}
    	}
    	return null;
    }

    // 디데이 일수 조회
    public int getDday(CoupleEntity couple) {
        LocalDate anniversaryDate = couple.getAnniversaryDate();
        LocalDate today = LocalDate.now();
        int dday = (int) ChronoUnit.DAYS.between(anniversaryDate, today)+1;
        return dday;
    }
    
	/******* 커플 비공개 설정 *******/
	public void updateIsPrivate(CoupleEntity coupleEntity) {	
		coupleEntity.setIsPrivate(true);
		coupleRepository.save(coupleEntity);	
	}
	
	/******* 전체 공개 설정 *******/
	public void updatePublic(CoupleEntity coupleEntity) {	
		coupleEntity.setIsPrivate(false);
		coupleRepository.save(coupleEntity);	
	}

}
