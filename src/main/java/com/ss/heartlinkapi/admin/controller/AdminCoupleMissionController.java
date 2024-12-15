package com.ss.heartlinkapi.admin.controller;

import com.ss.heartlinkapi.admin.service.AdminCoupleMissionService;
import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.linktag.repository.LinkTagRepository;
import com.ss.heartlinkapi.mission.dto.LinkMissionDTO;
import com.ss.heartlinkapi.mission.entity.LinkMissionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@RequestMapping("/admin")
public class AdminCoupleMissionController {

    //임시 나중에 서비스 생기면 서비스껄로 가져오기
    @Autowired
    private LinkTagRepository linkTagRepository;

    @Autowired
    private AdminCoupleMissionService adminMissionService;

    // 링크 미션 태그 추가
    @PostMapping("/missionLink")
    public ResponseEntity<?> addMissionTag(@RequestBody LinkMissionDTO missionTag) {
        try{

            if(missionTag == null || missionTag.getMissionTagName().isEmpty()){
                return ResponseEntity.badRequest().body("태그를 입력해주세요.");
            }

            LinkTagEntity result = linkTagRepository.findAllByKeyword(missionTag.getMissionTagName().trim());

            if (result != null){
                // 기존 태그를 가져와서 넣기
                LinkMissionEntity addResult = adminMissionService.addMissionTag(result, missionTag);
                if(addResult == null){
                    return ResponseEntity.badRequest().body("당월의 미션이 9개거나 이미 미션에 존재하는 태그입니다.");
                } else {
                    return ResponseEntity.ok(addResult);
                }
            } else {
                // 태그 명 새로 만들기
                LinkTagEntity linkTagEntity = new LinkTagEntity();
                linkTagEntity.setKeyword(missionTag.getMissionTagName().trim());
                LinkTagEntity addTagResult = linkTagRepository.save(linkTagEntity);
                LinkMissionEntity addResult = adminMissionService.addMissionTag(addTagResult, missionTag);
                if(addResult == null){
                    return ResponseEntity.badRequest().body("당월의 미션이 9개거나 이미 미션에 존재하는 태그입니다.");
                } else {
                    return ResponseEntity.ok(addResult);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // 링크 미션 태그 수정
    @Transactional
    @PutMapping("/missionslink/update/{missionId}")
    public ResponseEntity<?> updateMissionTag(@PathVariable Long missionId, @RequestBody LinkMissionDTO missionTag) {
        try{
            if(missionId == null || missionTag == null){
                return ResponseEntity.badRequest().build();
            }
            LinkMissionEntity mission = adminMissionService.findByMissionId(missionId);
            LinkMissionEntity result = adminMissionService.updateMission(mission, missionTag);
        if (result == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 링크 미션 태그 삭제
    @DeleteMapping("/missionslink/delete/{missionId}")
    public ResponseEntity<?> deleteMissionTag(@PathVariable Long missionId) {
        try{
            LinkMissionEntity findMission = adminMissionService.findByMissionId(missionId);
            if(findMission == null){
                return ResponseEntity.badRequest().build();
            }
            adminMissionService.deleteMissionTagById(findMission);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

}
