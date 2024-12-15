package com.ss.heartlinkapi.mission.service;

import com.ss.heartlinkapi.contentLinktag.entity.ContentLinktagEntity;
import com.ss.heartlinkapi.contentLinktag.repository.ContentLinktagRepository;
import com.ss.heartlinkapi.couple.entity.CoupleEntity;
import com.ss.heartlinkapi.couple.repository.CoupleRepository;
import com.ss.heartlinkapi.couple.service.CoupleService;
import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.linktag.repository.LinkTagRepository;
import com.ss.heartlinkapi.mission.dto.CompleteMissionDTO;
import com.ss.heartlinkapi.mission.entity.LinkMissionEntity;
import com.ss.heartlinkapi.mission.entity.UserLinkMissionEntity;
import com.ss.heartlinkapi.mission.repository.CoupleMissionRepository;
import com.ss.heartlinkapi.mission.repository.UserLinkMissionRepository;
import com.ss.heartlinkapi.post.dto.PostDTO;
import com.ss.heartlinkapi.post.entity.PostEntity;
import com.ss.heartlinkapi.post.entity.PostFileEntity;
import com.ss.heartlinkapi.post.repository.PostFileRepository;
import com.ss.heartlinkapi.post.repository.PostRepository;
import com.ss.heartlinkapi.user.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoupleMissionService {

    @Autowired
    private CoupleMissionRepository missionRepository;

    @Autowired
    private LinkTagRepository linkTagRepository;

    @Autowired
    private UserLinkMissionRepository userLinkMissionRepository;

    @Autowired
    private CoupleRepository coupleRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private ContentLinktagRepository contentLinktagRepository;

    @Autowired
    private PostFileRepository postFileRepository;

    @Autowired
    private CoupleService coupleService;

    // 매월 미션 태그 리스트 조회
    public List<LinkMissionEntity> findMissionByYearMonth(Integer year, Integer month) {
        return missionRepository.findMissionByYearMonth(year, month);
    }

    // 매월 미션 태그 리스트의 태그 객체 조회
    public List<Map<String, Object>> findMissionTag(List<LinkMissionEntity> missionList) {
        List<Map<String, Object>> tagList = new ArrayList<>();
        for (LinkMissionEntity missionEntity : missionList) {
            LinkTagEntity tag = linkTagRepository.findById(missionEntity.getLinkTagId().getId()).orElse(null);
            Map<String, Object> tagMap = new HashMap<>();
            tagMap.put("missionId", tag.getId());
            tagMap.put("linkTag", tag.getKeyword());
            tagList.add(tagMap);
        }
        return tagList;
    }

    // 이번 미션 태그에 달성되는지 확인하는 메서드
    // 게시글 작성 기능 완성 후 반환값 확인 필요
    public void checkMissionTag(LocalDateTime postDate, List<ContentLinktagEntity> postTag){
        // 지금 올린 피드의 날짜와 연결_링크태그 엔티티를 조회해온다.
        if(postTag != null) {
            // 연결_링크태그 테이블을 타고 링크 태그 테이블에서 그 태그들을 조회해온다.
            List<LinkTagEntity> tagList = new ArrayList<>();
            for (ContentLinktagEntity linkTagEntity : postTag) {
                LinkTagEntity tag = linkTagRepository.findById(linkTagEntity.getLinktagId().getId()).orElse(null);
                tagList.add(tag);
            }

            // 미션 테이블에서 이번달 태그 리스트를 가져온다.
            int year = postDate.getYear();
            int month = postDate.getMonthValue();
            List<LinkMissionEntity> missionList = missionRepository.findMissionByYearMonth(year, month);

            // 조회해 온 태그들의 아이디 중에서 이번 달의 미션 테이블의 태그 아이디와 동일한지 확인
            LinkMissionEntity findMission = null;
            for (LinkTagEntity tagItem : tagList) {
                for (LinkMissionEntity missionItem : missionList) {
                    if (tagItem.getId() == missionItem.getLinkTagId().getId()) {
                        findMission = missionItem;
                    }
                }
            }

            if (findMission == null) {
                // 미션 실패
            } else {
                // 미션 성공
                // 유저 아이디로 해당된 커플을 조회해옴.
                CoupleEntity couple = coupleRepository.findCoupleByUserId(postTag.get(0).getBoardId().getUserId().getUserId());

                // 유저 링크 미션 테이블에 추가
                UserLinkMissionEntity linkMission = new UserLinkMissionEntity();
                linkMission.setCoupleId(couple);
                linkMission.setLinkMissionId(findMission);
                linkMission.setStatus(true);
                UserLinkMissionEntity missionOk = userLinkMissionRepository.save(linkMission);
                if (missionOk != null) {
                    System.out.println("링크 미션 성공");
                } else {
                    System.out.println("링크 미션 실패");
                }
            }
        } else {
            System.out.println("설정한 태그가 없습니다.");
        }

    }

    // 유저 미션 테이블에서 커플 아이디로 조회하기
    public List<UserLinkMissionEntity> findUserLinkMissionByCoupleId(CoupleEntity couple) {
        return userLinkMissionRepository.findAllByCoupleId(couple);
    }

    // 유저 미션 삭제
    public void deleteUserMissionByCoupleId(CoupleEntity couple) {
        userLinkMissionRepository.deleteAllByCoupleId(couple);
    }

    // 모든 미션 리스트 조회
    public List<LinkMissionEntity> findAllMissions() {
        return missionRepository.findAll();
    }

    // 미션태그 아이디로 미션 태그 entity 조회
    public LinkMissionEntity findOneMissionTag(Long missionId) {
        return missionRepository.findById(missionId).orElse(null);
    }

    // 미션태그 아이디로 태그 entity 조회
    public LinkTagEntity findTagByMissionId(Long missionId) {
        LinkMissionEntity mission = missionRepository.findById(missionId).orElse(null);
        return linkTagRepository.findById(mission.getLinkTagId().getId()).orElse(null);
    }

    // 미션태그 아이디를 가지고 글 작성 페이지로 이동 시 태그 넘겨주기
    public PostDTO writePostWithTag(LinkTagEntity tag){
        PostDTO post = new PostDTO();
        post.setContent("&"+tag.getKeyword()+" ");
        return post;
    }

    // 유저 아이디로 완료된 미션태그 조회
    public List<CompleteMissionDTO> getMissionStatus(Long userId, Integer year, Integer month) {

        // 넘어온 날짜가 없을 경우 디폴트값 현재
        if(year == null){
            year = LocalDate.now().getYear();
        }
        if(month == null){
            month = LocalDate.now().getMonthValue();
        }

        // 유저아이디로 유저 게시글 작성시간을 기준으로 월로 조회해서 전부 가져오기
        List<PostEntity> postList = postRepository.findAllByUserIdAndMonth(userId, year, month);

        // 이번달의 태그 아이디들 조회해오기
        List<LinkMissionEntity> missionList = findMissionByYearMonth(year, month);

        // 유저 게시글들에 연결된 태그들 조회하기
        Set<Map<String, Object>> tagList = new HashSet<>();

        List<CompleteMissionDTO> myMissionList = new ArrayList<>();

        for(PostEntity postEntity : postList){
            // 유저가 작성한 게시글들의 아이디로 정보 조회
            List<ContentLinktagEntity> contentList = contentLinktagRepository.findByBoardId(postEntity);
            for(ContentLinktagEntity contentLinktagEntity : contentList){
                // 게시글들의 태그 아이디와 미션태그의 태그 아이디를 비교해서 맞으면 저장
                for(LinkMissionEntity mission : missionList){
                    if(contentLinktagEntity.getLinktagId()==mission.getLinkTagId()){
                        // 이번달의 태그와 이번달 작성한 게시글의 태그가 맞을 때
                        // 포스트 아이디로 포스트 이미지 조회
                        List<PostFileEntity> fileList = postFileRepository.findByPostId(contentLinktagEntity.getBoardId().getPostId());
                        CompleteMissionDTO myMission = new CompleteMissionDTO();
                        myMission.setMissionId(mission.getLinkMissionId());
                        myMission.setLinkTagId(mission.getLinkTagId().getId());
                        myMission.setKeyword(mission.getLinkTagId().getKeyword());
                        myMission.setPostId(contentLinktagEntity.getBoardId().getPostId());
                        if(fileList.size()<1 || fileList.get(0)==null){
                            myMission.setPostImgUrl("이미지가 없습니다.");
                        } else {
                            myMission.setPostImgUrl(fileList.get(0).getFileUrl());
                        }
                        myMissionList.add(myMission);
                    }
                }
            }
        }

        // 상대방 아이디로 유저 게시글 작성시간을 기준으로 월로 조회해서 전부 가져오기
        UserEntity partner;
        CoupleEntity couple = coupleService.findByUser1_IdOrUser2_Id(userId);
        if(couple.getUser1().getUserId()==userId) {
            partner = couple.getUser2();
        } else {
            partner = couple.getUser1();
        }
        List<PostEntity> partnerPostList = postRepository.findAllByUserIdAndMonth(partner.getUserId(), year, month);
        System.out.println("partnerPostList : "+partnerPostList);

        List<CompleteMissionDTO> partnerMissionList = new ArrayList<>();

        for(PostEntity postEntity : partnerPostList){
            // 상대방이 작성한 게시글들의 아이디로 정보 조회
            List<ContentLinktagEntity> contentList = contentLinktagRepository.findByBoardId(postEntity);
            for(ContentLinktagEntity contentLinktagEntity : contentList){
                // 게시글들의 태그 아이디와 미션태그의 태그 아이디를 비교해서 맞으면 저장
                for(LinkMissionEntity mission : missionList){
                    if(contentLinktagEntity.getLinktagId()==mission.getLinkTagId()){
                        // 이번달의 태그와 이번달 작성한 게시글의 태그가 맞을 때
                        // 포스트 아이디로 포스트 이미지 조회
                        List<PostFileEntity> fileList = postFileRepository.findByPostId(contentLinktagEntity.getBoardId().getPostId());
                        CompleteMissionDTO myMission = new CompleteMissionDTO();
                        myMission.setMissionId(mission.getLinkMissionId());
                        myMission.setLinkTagId(mission.getLinkTagId().getId());
                        myMission.setKeyword(mission.getLinkTagId().getKeyword());
                        myMission.setPostId(contentLinktagEntity.getBoardId().getPostId());
                        if(fileList.size()<1 || fileList.get(0)==null){
                            myMission.setPostImgUrl("이미지가 없습니다.");
                        } else {
                            myMission.setPostImgUrl(fileList.get(0).getFileUrl());
                        }
                        partnerMissionList.add(myMission);
                    }
                }
            }
        }

        // 상대방이 완료한 미션이 있는지 확인하고 있으면 추가하고 겹치면 추가 안하기
        // 1. myMissionList의 linktagId를 Set에 저장
        Set<Long> myMissionLinktagIds = myMissionList.stream()
                .map(CompleteMissionDTO::getLinkTagId)
                .collect(Collectors.toSet());

        // 2. partnerMissionList를 순회하며 myMissionList에 없는 linktagId를 추가
        for (CompleteMissionDTO partnerMission : partnerMissionList) {
            if (!myMissionLinktagIds.contains(partnerMission.getLinkTagId())) {
                myMissionList.add(partnerMission); // 없는 linktagId의 partnerMission을 myMissionList에 추가
                myMissionLinktagIds.add(partnerMission.getLinkTagId()); // 중복 방지를 위해 Set에도 추가
            }
        }


        if (myMissionList.isEmpty()) {
            return new ArrayList<>();
        }

        return myMissionList;
    }
}
