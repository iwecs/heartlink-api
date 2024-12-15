package com.ss.heartlinkapi.elasticSearch.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ss.heartlinkapi.elasticSearch.document.ElasticTagDocument;
import com.ss.heartlinkapi.elasticSearch.document.ElasticUserDocument;
import com.ss.heartlinkapi.elasticSearch.document.SearchHistoryDocument;
import com.ss.heartlinkapi.elasticSearch.repository.ElasticHistoryRepository;
import com.ss.heartlinkapi.elasticSearch.repository.ElasticTagInfoRepository;
import com.ss.heartlinkapi.elasticSearch.repository.ElasticUserInfoRepository;
import com.ss.heartlinkapi.linktag.entity.LinkTagEntity;
import com.ss.heartlinkapi.search.entity.SearchHistoryEntity;
import com.ss.heartlinkapi.user.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ElasticService {

    private final ElasticHistoryRepository elasticHistoryRepository;
    private final ElasticUserInfoRepository userInfoRepository;
    private final ElasticTagInfoRepository tagInfoRepository;
    private final ElasticsearchClient elasticsearchClient;
    private final DeepLService deepLService;

    public ElasticService(ElasticHistoryRepository elasticHistoryRepository, ElasticUserInfoRepository userInfoRepository, ElasticTagInfoRepository tagInfoRepository, ElasticsearchClient elasticsearchClient, DeepLService deepLService) {
        this.elasticHistoryRepository = elasticHistoryRepository;
        this.userInfoRepository = userInfoRepository;
        this.tagInfoRepository = tagInfoRepository;
        this.elasticsearchClient = elasticsearchClient;
        this.deepLService = deepLService;
    }

    // 검색기록 추가
    public SearchHistoryDocument addOrUpdateHistory(SearchHistoryEntity historyEntity) {
        System.out.println(historyEntity);
        SearchHistoryDocument historyDocument = findHistoryById(historyEntity.getSearchHistoryId());
        if (historyDocument == null) {
            // mysql의 검색기록이 인덱스 안에 없을 때
            historyDocument = new SearchHistoryDocument();
            historyDocument.setSearchHistoryId(historyEntity.getSearchHistoryId());
            historyDocument.setDate(historyEntity.getCreatedAt());
            historyDocument.setType(historyEntity.getType());
            historyDocument.setKeyword(historyEntity.getKeyword());
            historyDocument.setUserId(historyEntity.getUserId().getUserId());
            return elasticHistoryRepository.save(historyDocument);
        } else {
            // mysql의 검색기록이 인덱스 안에 있을 때
            historyDocument.setDate(historyEntity.getUpdatedAt());
            return elasticHistoryRepository.save(historyDocument);
        }
    }

    // mysql 테이블에 있는 검색기록이 도큐먼트에 있는지 확인
    private SearchHistoryDocument findHistoryById(Long historyEntityId) {
        return elasticHistoryRepository.findById(historyEntityId).orElse(null);
    }

    // 인덱스에서 유저 아이디로 최근 검색 순으로 검색기록 찾기
    public List<SearchHistoryDocument> findByUserId(Long userId) {
        List<SearchHistoryDocument> searchList = elasticHistoryRepository.findByUserIdOrderByDateDesc(userId);
        return searchList;
    }

    // 유저 추가
    public ElasticUserDocument addUser(UserEntity userEntity) {
        ElasticUserDocument elasticUserDocument = new ElasticUserDocument();
        elasticUserDocument.setUserId(userEntity.getUserId());
        elasticUserDocument.setLoginId(userEntity.getLoginId());
        elasticUserDocument.setName(userEntity.getName());
        return userInfoRepository.save(elasticUserDocument);
    }

    // 태그 추가
    public ElasticTagDocument addTag(LinkTagEntity tagEntity) {
        ElasticTagDocument elasticTagDocument = new ElasticTagDocument();
        elasticTagDocument.setEngTagName(deepLService.translate(tagEntity.getKeyword(), Language.KO, Language.EN));
        elasticTagDocument.setKorTagName(deepLService.translate(tagEntity.getKeyword(), Language.EN, Language.KO));
        elasticTagDocument.setTagName(tagEntity.getKeyword());
        elasticTagDocument.setTagId(tagEntity.getId());
        return tagInfoRepository.save(elasticTagDocument);
    }

    // 아이디 자동완성
    public List<Map<String, Object>> idAutoComplete(String prefix) throws Exception {
        // Prefix를 사용한 쿼리 생성
        SearchRequest searchRequest = SearchRequest.of(s -> s
                .index(IndexClass.USER_INDEX_NAME)  // 검색할 인덱스 이름 지정
                .query(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .prefix(p -> p
                                                .field("loginId") // 검색할 필드 지정
                                                .value(prefix) // 입력된 접두사
                                        )
                                )
                        )
                )
        );

        // 검색 요청 수행
        SearchResponse<ElasticUserDocument> response;

        try {
            response = elasticsearchClient.search(searchRequest, ElasticUserDocument.class);
        } catch (Exception e) {
            System.err.println("엘라스틱 자동완성 검색 실패 : " + e.getMessage());
            return List.of();  // 빈 리스트 반환
        }

        List<Hit<ElasticUserDocument>> hits = response.hits().hits();

        List<ElasticUserDocument> userList = hits.stream()
                .map(hit -> hit.source())  // loginId 필드 추출
                .collect(Collectors.toList());

        System.out.println(userList);

        List<Map<String, Object>> userMapList = new ArrayList<>();
        for(ElasticUserDocument userDoc : userList) {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("loginId", userDoc.getLoginId());
            userMap.put("name", userDoc.getName());
            userMap.put("userId", userDoc.getUserId());
            userMapList.add(userMap);
        }

        return userMapList;
    }

    // 태그 자동완성
    public List<Map<String, Object>> tagAutoComplete(String searchTag) throws Exception {
        String fieldToSearch;
        SearchRequest searchRequest;
        boolean isKorean;
        if (searchTag != null && !searchTag.isEmpty()) {
            char firstChar = searchTag.charAt(0);
            if (Character.UnicodeScript.of(firstChar) == Character.UnicodeScript.HANGUL) {
                fieldToSearch = "kor_TagName"; // 첫 글자가 한글인 경우
                isKorean = true;
            } else if (Character.UnicodeScript.of(firstChar) == Character.UnicodeScript.LATIN) {
                fieldToSearch = "eng_TagName"; // 첫 글자가 영어인 경우
                isKorean = false;
            } else {
                fieldToSearch = "kor_TagName";
                isKorean = true;
            }
        } else {
            return List.of();
        }

        if(isKorean) {
            searchRequest = SearchRequest.of(s -> s
                    .index(IndexClass.TAG_INDEX_NAME)
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m
                                            .prefix(p -> p
                                                    .field("kor_TagName")
                                                    .value(searchTag) // 입력된 한 글자
                                            )
                                    )
                            )
                    )
            );
        } else {
            searchRequest = SearchRequest.of(s -> s
                    .index(IndexClass.TAG_INDEX_NAME)
                    .query(q -> q
                            .bool(b -> b
                                    .must(m -> m
                                            .match(mq -> mq
                                                    .field(fieldToSearch)
                                                    .query(searchTag)
                                                    .fuzziness("AUTO")
                                            )
                                    )
                            )
                    )
            );
        }

        // 검색 요청 수행
        SearchResponse<ElasticTagDocument> response;

        try {
            response = elasticsearchClient.search(searchRequest, ElasticTagDocument.class);
        } catch (Exception e) {
            System.err.println("엘라스틱 자동완성 검색 실패 : " + e.getMessage());
            return List.of();  // 빈 리스트 반환
        }

        List<Hit<ElasticTagDocument>> hits = response.hits().hits();

        List<ElasticTagDocument> tagList = hits.stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());


        List<Map<String, Object>> tagMapList = new ArrayList<>();
        for(ElasticTagDocument tagDoc : tagList) {
            Map<String, Object> tagMap = new HashMap<>();
            tagMap.put("tagId", tagDoc.getTagId());
            tagMap.put("tagName", tagDoc.getTagName());
            tagMapList.add(tagMap);
        }
        return tagMapList;
    }

}
