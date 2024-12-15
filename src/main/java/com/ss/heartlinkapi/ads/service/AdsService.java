package com.ss.heartlinkapi.ads.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ss.heartlinkapi.ads.dto.EbayProductDTO;
import com.ss.heartlinkapi.elasticSearch.document.SearchHistoryDocument;
import com.ss.heartlinkapi.elasticSearch.service.DeepLService;
import com.ss.heartlinkapi.elasticSearch.service.ElasticService;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

@Service
public class AdsService {

    private final ElasticService elasticService;
    private final RestTemplate restTemplate;
    private HttpHeaders headers;
    private String token;

    public AdsService(ElasticService elasticService, DeepLService deepLService) {
        this.elasticService = elasticService;
        this.restTemplate = new RestTemplate();
        this.headers = new HttpHeaders();
    }

    public void getPickAds(String url){
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        System.out.println(response.getBody());
    }

    // 회원 아이디로 광고 가져오기
    public List<Map<String, Object>> getAds(Long userId) {

        List<SearchHistoryDocument> searchList = elasticService.findByUserId(userId);
        List<String> historyList = new ArrayList<>();

        if(searchList.isEmpty()) {
            historyList.add("gift");
            historyList.add("watch");
            historyList.add("bag");
        } else {
            for (SearchHistoryDocument history : searchList) {
                String[] keywords = history.getKeyword().split(" "); // 키워드 쪼개기
                historyList.addAll(Arrays.asList(keywords)); // 쪼갠 키워드를 리스트에 추가
            }
        }
        String adsList = getAdsList(historyList);
        List<EbayProductDTO> itemList = parseItems(adsList);
        List<Map<String, Object>> itemMapList = new ArrayList<>();
        for(EbayProductDTO item : itemList) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("title", item.getTitle());
            itemMap.put("imgUrl", item.getImgUrl());
            itemMap.put("siteUrl", item.getSiteUrl());
            itemMap.put("currency", item.getCurrency());
            itemMap.put("price", item.getPrice());
            itemMap.put("viewCount", item.getViewCount());
            itemMap.put("searchTime", item.getSearchTime());
            itemMapList.add(itemMap);
        }
        return itemMapList;
    }

    // 키워드를 넘겨서 광고 상품 목록 받아오기
    private String getAdsList(List<String> keywords){
        final String EBAY_GET_URL = "https://svcs.ebay.com/services/search/FindingService/v1";
        final String APPKEY = "-HeartLin-PRD-7b15e11a8-28f07714";
        int getItemCount = 30; // 가져올 아이템 갯수


        headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-EBAY-SOA-OPERATION-NAME", "findItemsByKeywords");
        headers.set("X-EBAY-SOA-SECURITY-APPNAME", APPKEY);
        headers.set("X-EBAY-SOA-REST-PAYLOAD", "true");
        headers.set("X-EBAY-SOA-RESPONSE-DATA-FORMAT", "JSON");

        String url = String.format(
                "%s?OPERATION-NAME=findItemsByKeywords&SECURITY-APPNAME=%s&RESPONSE-DATA-FORMAT=JSON&REST-PAYLOAD=true&keywords=%s&paginationInput.entriesPerPage="+getItemCount+"&paginationInput.pageNumber=1",
                EBAY_GET_URL, APPKEY, keywords.get(0)
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return response.getBody();
    }

    // 이베이 토큰 생성 (배치 프로그램으로 1시간 단위로 계속 발급)
    @PostConstruct
    public void getAdsToken() {
        final String EBAY_GETTOKEN_URL = "https://api.ebay.com/identity/v1/oauth2/token";
        final String EBAY_GETTOKEN_APPKEY = "LUhlYXJ0TGluLVBSRC03YjE1ZTExYTgtMjhmMDc3MTQ6UFJELWIxNWUxMWE4YWJjYS0zNWNhLTQ2MzYtOGI5NC02NDZi";

        headers = new HttpHeaders();
        headers.set("Authorization", "Basic "+EBAY_GETTOKEN_APPKEY);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 변경된 콘텐츠 타입

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");
        body.add("scope", "https://api.ebay.com/oauth/api_scope");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(EBAY_GETTOKEN_URL, HttpMethod.POST, entity, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        token = jsonNode.get("access_token").asText();
        System.out.println("이베이 토큰 발급 : "+token);
    }

    // 이베이 상품 정보 파싱
    private List<EbayProductDTO> parseItems(String jsonResponse) {
        List<EbayProductDTO> products = new ArrayList<>();

        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);

            JSONArray findItemsByKeywordsResponseArray = (JSONArray)jsonObject.get("findItemsByKeywordsResponse");
            JSONObject findItemsByKeywordsResponse = (JSONObject)findItemsByKeywordsResponseArray.get(0);
            JSONArray searchResultArray = (JSONArray)findItemsByKeywordsResponse.get("searchResult");
            JSONObject searchResult = (JSONObject)searchResultArray.get(0);

            JSONArray timestampArray = (JSONArray)findItemsByKeywordsResponse.get("timestamp");
            String timestamp = (String)timestampArray.get(0);
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(timestamp);
            // 검색 조회한 시간
            LocalDateTime searchTime = zonedDateTime.toLocalDateTime();

            // 가져온 아이템 개수
            int count = Integer.parseInt((String)searchResult.get("@count"));

            JSONArray itemArray = (JSONArray)searchResult.get("item");
            for(int i = 0; i<itemArray.size(); i++) {
                JSONObject item = (JSONObject)itemArray.get(i);
                JSONArray titleWrap = (JSONArray)item.get("title");
                // 상품명
                String title = (String) titleWrap.get(0);
                JSONArray imgURLWrap = (JSONArray)item.get("galleryURL");
                // 이미지 경로
                String imgUrl = (String) imgURLWrap.get(0);
                JSONArray siteURLWrap = (JSONArray)item.get("viewItemURL");
                // 상세페이지 경로
                String siteUrl = (String) siteURLWrap.get(0);
                JSONArray sellingStatusWrap = (JSONArray)item.get("sellingStatus");
                JSONObject sellingStatus2Wrap = (JSONObject)sellingStatusWrap.get(0);
                JSONArray sellingStatus3Wrap = (JSONArray)sellingStatus2Wrap.get("currentPrice");
                JSONObject sellingStatus4Wrap = (JSONObject)sellingStatus3Wrap.get(0);
                // 화폐 단위
                String currency = (String)sellingStatus4Wrap.get("@currencyId");
                // 가격
                double price = Double.parseDouble((String)sellingStatus4Wrap.get("__value__"));
                JSONArray listingInfoWrap = (JSONArray)item.get("listingInfo");
                JSONObject listingInfo = (JSONObject)listingInfoWrap.get(0);
                // 조회 수
                EbayProductDTO productDTO = new EbayProductDTO();
                productDTO.setTitle(title);
                productDTO.setImgUrl(imgUrl);
                productDTO.setSiteUrl(siteUrl);
                productDTO.setPrice(price);
                productDTO.setCurrency(currency);
                productDTO.setSearchTime(searchTime);
                products.add(productDTO);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;

    }


}
