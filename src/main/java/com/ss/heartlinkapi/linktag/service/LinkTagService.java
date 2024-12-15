package com.ss.heartlinkapi.linktag.service;

import com.ss.heartlinkapi.elasticSearch.service.ElasticService;
import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.linktag.repository.LinkTagRepository;
import org.springframework.stereotype.Service;

@Service
public class LinkTagService {

    private final LinkTagRepository linkTagRepository;
    private final ElasticService elasticService;

    public LinkTagService(LinkTagRepository linkTagRepository, ElasticService elasticService) {
        this.linkTagRepository = linkTagRepository;
        this.elasticService = elasticService;
    }

    // 태그 저장
    public LinkTagEntity saveTag(String tagName) {
        LinkTagEntity isSavedTag = linkTagRepository.findAllByKeyword(tagName);
        if (isSavedTag == null) {
            LinkTagEntity linkTagEntity = new LinkTagEntity();
            linkTagEntity.setKeyword(tagName);
            LinkTagEntity result = linkTagRepository.save(linkTagEntity);
            // elastic 태그 저장
            if(elasticService.addTag(result)==null) {
                System.out.println("엘라스틱 태그 저장 실패");
            }
            return result;
        } else {
            return isSavedTag;
        }
    }
}
