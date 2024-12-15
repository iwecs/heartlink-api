package com.ss.heartlinkapi.admin.service;

import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.linktag.repository.LinkTagRepository;
import com.ss.heartlinkapi.mission.dto.LinkMissionDTO;
import com.ss.heartlinkapi.mission.entity.LinkMissionEntity;
import com.ss.heartlinkapi.mission.repository.CoupleMissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class AdminCoupleMissionService {

    @Autowired
    private CoupleMissionRepository missionRepository;

    // 임시. 나중에 서비스로 바꾸기
    @Autowired
    private LinkTagRepository linkTagRepository;

    // 링크 미션 태그 추가
    public LinkMissionEntity addMissionTag(LinkTagEntity linkTag, LinkMissionDTO linkMissionDTO) {
        List<LinkMissionEntity> thisMissions = missionRepository.findMissionByYearMonth(linkMissionDTO.getYear(), linkMissionDTO.getMonth());
        boolean checkMission = false;
        for(LinkMissionEntity linkMissionEntity : thisMissions) {
            if(linkMissionEntity.getLinkTagId().getId()==linkTag.getId()){
                checkMission = true;
            }
        }
        if (thisMissions.size()<=8 && !checkMission) {
            LinkMissionEntity linkMissionEntity = new LinkMissionEntity();
            linkMissionEntity.setLinkTagId(linkTag);
            LocalDate startDate = LocalDate.of(linkMissionDTO.getYear(), linkMissionDTO.getMonth(), 1);
            int lastDay = YearMonth.of(linkMissionDTO.getYear(), linkMissionDTO.getMonth()).atEndOfMonth().getDayOfMonth();
            LocalDate endDate = LocalDate.of(linkMissionDTO.getYear(), linkMissionDTO.getMonth(), lastDay);

            linkMissionEntity.setStart_date(startDate);
            linkMissionEntity.setEnd_date(endDate);
            LinkMissionEntity result = missionRepository.save(linkMissionEntity);
            return result;
        } else {
            return null;
        }
    }

    // 미션 아이디로 미션 찾기
    public LinkMissionEntity findByMissionId(Long missionId) {
        LinkMissionEntity why = missionRepository.findById(missionId).orElse(null);
        return why;
    }

    // 미션 태그 수정
    public LinkMissionEntity updateMission(LinkMissionEntity beforeMission, LinkMissionDTO afterMission) {
        LocalDate startDate = LocalDate.of(afterMission.getYear(), afterMission.getMonth(), 1);
        int lastDay = YearMonth.of(afterMission.getYear(), afterMission.getMonth()).atEndOfMonth().getDayOfMonth();
        LocalDate endDate = LocalDate.of(afterMission.getYear(), afterMission.getMonth(), lastDay);
        beforeMission.setStart_date(startDate);
        beforeMission.setEnd_date(endDate);

        LinkTagEntity findTag = linkTagRepository.findAllByKeyword(afterMission.getMissionTagName());

        if(findTag != null) {
            // 기존 태그가 존재할 때 기존 태그 사용
            beforeMission.setLinkTagId(findTag);
            return missionRepository.save(beforeMission);
        } else {
            // 기존에 태그가 존재하지 않을 때 새 태그 추가
            LinkTagEntity newTag = new LinkTagEntity();
            newTag.setKeyword(afterMission.getMissionTagName());
            linkTagRepository.save(newTag);
            beforeMission.setLinkTagId(newTag);
            return missionRepository.save(beforeMission);
        }
    }

    // 미션 태그 삭제
    public void deleteMissionTagById(LinkMissionEntity missionId) {
        missionRepository.delete(missionId);
    }
}
