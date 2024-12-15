package com.ss.heartlinkapi.elasticSearch.repository;

import com.ss.heartlinkapi.elasticSearch.document.ElasticUserDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticUserInfoRepository extends ElasticsearchRepository<ElasticUserDocument, Long> {
}
