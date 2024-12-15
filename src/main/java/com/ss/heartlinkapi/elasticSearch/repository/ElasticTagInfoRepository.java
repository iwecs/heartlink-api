package com.ss.heartlinkapi.elasticSearch.repository;

import com.ss.heartlinkapi.elasticSearch.document.ElasticTagDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticTagInfoRepository extends ElasticsearchRepository<ElasticTagDocument, Long> {


}
