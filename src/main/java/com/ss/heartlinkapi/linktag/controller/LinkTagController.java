package com.ss.heartlinkapi.linktag.controller;

import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.linktag.service.LinkTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag")
public class LinkTagController {

    @Autowired
    private LinkTagService linkTagService;

    // 테스트용 태그 추가
    @PostMapping("/add")
    public ResponseEntity<?> addTag(@RequestParam String tagName){
        LinkTagEntity result = linkTagService.saveTag(tagName);
        return ResponseEntity.ok(result);
    }
}
