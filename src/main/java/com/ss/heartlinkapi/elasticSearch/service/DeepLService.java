package com.ss.heartlinkapi.elasticSearch.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class DeepLService {

    // 번역 DeepL API URL
    private static final String DEEPL_API_URL = "https://api-free.deepl.com/v2/translate";
    // 번역 DeepL API KEY
    private static final String AUTH_KEY = "04effbda-ae8c-4ce6-b384-78cc496fd41a:fx";

    // 번역 기능
    public String translate(String text, Language from, Language to) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "DeepL-Auth-Key "+AUTH_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("text", new String[]{text});
        body.put("source_lang", from);
        body.put("target_lang", to);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(DEEPL_API_URL, HttpMethod.POST, entity, String.class);

        String result = response.getBody();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(result);
            return jsonNode.get("translations").get(0).get("text").asText(); // 번역된 텍스트만 반환
        } catch (Exception e) {
            e.printStackTrace();
            return "Translation error"; // 오류 처리
        }

    }

}
