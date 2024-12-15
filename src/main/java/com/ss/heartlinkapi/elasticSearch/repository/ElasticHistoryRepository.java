package com.ss.heartlinkapi.elasticSearch.repository;

import com.ss.heartlinkapi.elasticSearch.document.SearchHistoryDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ElasticHistoryRepository extends ElasticsearchRepository<SearchHistoryDocument, Long> {

    // 인덱스에서 유저아이디로 검색기록 리스트 찾기
    public List<SearchHistoryDocument> findByUserIdOrderByDateDesc(Long userId);

}
