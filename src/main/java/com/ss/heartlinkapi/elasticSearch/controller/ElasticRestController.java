package com.ss.heartlinkapi.elasticSearch.controller;

import com.ss.heartlinkapi.elasticSearch.document.ElasticTagDocument;
import com.ss.heartlinkapi.elasticSearch.document.ElasticUserDocument;
import com.ss.heartlinkapi.elasticSearch.document.SearchHistoryDocument;
import com.ss.heartlinkapi.elasticSearch.service.ElasticIndexService;
import com.ss.heartlinkapi.elasticSearch.service.ElasticService;
import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.linktag.service.LinkTagService;
import com.ss.heartlinkapi.user.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.POST;

import java.util.List;


@RestController
@RequestMapping("/es")
public class ElasticRestController {

    @Autowired
    private ElasticService elasticService;

    @Autowired
    private LinkTagService linkTagService;

    // 테스트 조회
    @GetMapping("/test")
    public ResponseEntity<?> test(@RequestParam Long userId){
        List<SearchHistoryDocument> result = elasticService.findByUserId(userId);
        System.out.println(result);
        return ResponseEntity.ok(result);
    }

    // 테스트 유저 추가
    @PostMapping("/addUser")
    public ResponseEntity<?> testAddUser(@RequestBody UserEntity userEntity){
        ElasticUserDocument result = elasticService.addUser(userEntity);
        return ResponseEntity.ok(result);
    }

    // 테스트 태그 추가
    @PostMapping("/addTag")
    public ResponseEntity<?> testAddTag(@RequestParam String tagName){
        LinkTagEntity result = linkTagService.saveTag(tagName);
        return ResponseEntity.ok(result);
    }

    // 아이디 자동완성 기능
    @GetMapping("/idAuto")
    public ResponseEntity<?> idAutoComplete(@RequestParam String searchId){
        try {
            return ResponseEntity.ok(elasticService.idAutoComplete(searchId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // 빈 리스트 반환
        }
    }

    // 태그 자동완성 기능
    @GetMapping("/tagAuto")
    public ResponseEntity<?> tagAutoComplete(@RequestParam String searchTag){
        try {
            return ResponseEntity.ok(elasticService.tagAutoComplete(searchTag));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(List.of()); // 빈 리스트 반환
        }
    }
}
