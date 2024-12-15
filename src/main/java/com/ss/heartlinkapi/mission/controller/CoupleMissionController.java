package com.ss.heartlinkapi.mission.controller;

import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.login.dto.CustomUserDetails;
import com.ss.heartlinkapi.mission.dto.CompleteMissionDTO;
import com.ss.heartlinkapi.mission.entity.LinkMissionEntity;
import com.ss.heartlinkapi.mission.service.CoupleMissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/couple")
public class CoupleMissionController {

    @Autowired
    private CoupleMissionService missionService;

    // 모든 미션이 있는 연, 월 조회
    @GetMapping("/missionAllList")
    public ResponseEntity<?> selectAllMissionDate(){
        List<LinkMissionEntity> allMissions = missionService.findAllMissions();

        Set<Integer> years = new HashSet<>();
        Set<Integer> months = new HashSet<>();

        for(LinkMissionEntity linkMissionEntity : allMissions){
            years.add(linkMissionEntity.getStart_date().getYear());
            months.add(linkMissionEntity.getStart_date().getMonthValue());
        }

        Map<String, Set<Integer>> allMissionDate = new HashMap<>();
        allMissionDate.put("years", years);
        allMissionDate.put("months", months);

        return ResponseEntity.ok(allMissionDate);
    }

    // 매월 미션태그 조회
    @GetMapping("/missionslink")
    public ResponseEntity<?> selectMissionTags(@RequestParam(value = "year", required = false) Integer year, @RequestParam(value = "month", required = false) Integer month){
        try{

            // 넘어온 날짜가 없을 경우 디폴트값 현재
            if(year == null){
                 year = LocalDate.now().getYear();
             }
            if(month == null){
                month = LocalDate.now().getMonthValue();
            }

            // 매월 미션 리스트 조회
            List<LinkMissionEntity> missionList = missionService.findMissionByYearMonth(year, month);

            if(missionList == null || missionList.isEmpty()){
                return ResponseEntity.notFound().build();
            }
            // 미션 리스트의 태그 조회
            List<Map<String, Object>> tagList =  missionService.findMissionTag(missionList);

            return ResponseEntity.ok(tagList);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 미션 클릭 시 해당 미션태그 반환
    @GetMapping("/writeMission")
    public ResponseEntity<?> writeMission(@RequestParam Long missionId){
        try {
            if(missionId == null){
                return ResponseEntity.badRequest().build();
            }
            LinkTagEntity tag = missionService.findTagByMissionId(missionId);
            if(tag == null){
                return ResponseEntity.notFound().build();
            }
            missionService.writePostWithTag(tag);
            return ResponseEntity.ok(missionService.writePostWithTag(tag));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 커플의 완료된 미션 여부 반환
    @GetMapping("/missionStatus")
    public ResponseEntity<?> missionStatus(@AuthenticationPrincipal CustomUserDetails user
    ,@RequestParam(value = "year", required = false) Integer year, @RequestParam(value = "month", required = false) Integer month){
       try {
           if(user == null){
               return ResponseEntity.badRequest().build();
           }
           List<CompleteMissionDTO> completeList = missionService.getMissionStatus(user.getUserId(), year, month);
           return ResponseEntity.ok(completeList);
       } catch (Exception e) {
           e.printStackTrace();
           return ResponseEntity.internalServerError().build();
       }
    }
}
