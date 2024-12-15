package com.ss.heartlinkapi.elasticSearch.service;

public class IndexClass {

    public static final String INDEX_NAME = "search_history";
    public static final String MAPPING_FILE_PATH = "src/main/resources/elasticSearch/search_history_mapping.json";

    // 유저 인덱스
    public static final String USER_INDEX_NAME = "user_info";
    // 태그 인덱스
    public static final String TAG_INDEX_NAME = "tag_info";

    // url
    public static final String URL = "http://localhost:9200/";

    // 유저 인덱스 매핑+설정
    public static final String USERINDEX = "{\n" +
            "  \"settings\": {\n" +
            "    \"analysis\": {\n" +
            "      \"normalizer\": {\n" +
            "        \"my_normalizer\": {\n" +
            "          \"type\": \"custom\",\n" +
            "          \"filter\": [\"lowercase\"]\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"userId\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"loginId\": {\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"normalizer\": \"my_normalizer\"\n" +
            "      },\n" +
            "      \"name\": {\n" +
            "        \"type\": \"text\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    // 태그 인덱스 매핑+설정
    public static final String TAGINDEX = "{\n" +
            "  \"settings\": {\n" +
            "    \"analysis\": {\n" +
            "      \"tokenizer\": {\n" +
            "        \"my-nori-tokenizer\": {\n" +
            "          \"type\": \"nori_tokenizer\",\n" +
            "          \"decompound_mode\": \"mixed\"\n" +
            "        },\n" +
            "        \"my-ngram-tokenizer\": {\n" +
            "          \"type\": \"ngram\",\n" +
            "          \"min_gram\": 2,\n" +
            "          \"max_gram\": 3,\n" +
            "          \"token_chars\": [\"letter\", \"digit\"]\n" +
            "        }\n" +
            "      },\n" +
            "      \"analyzer\": {\n" +
            "        \"my-nori-analyzer\": {\n" +
            "          \"type\": \"custom\",\n" +
            "          \"tokenizer\": \"my-nori-tokenizer\",\n" +
            "          \"filter\": [\n" +
            "            \"lowercase\",\n" +
            "            \"stop\",\n" +
            "            \"trim\",\n" +
            "            \"nori_part_of_speech\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"my-ngram-analyzer\": {\n" +
            "          \"type\": \"custom\",\n" +
            "          \"tokenizer\": \"my-ngram-tokenizer\",\n" +
            "          \"filter\": [\n" +
            "            \"lowercase\",\n" +
            "            \"trim\"\n" +
            "          ]\n" +
            "        },\n" +
            "        \"custom_analyzer\": {\n" +
            "          \"type\": \"custom\",\n" +
            "          \"tokenizer\": \"standard\",\n" +
            "          \"filter\": [\"lowercase\"]\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"tagId\": {\n" +
            "        \"type\": \"long\"\n" +
            "      },\n" +
            "      \"tagName\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"eng_TagName\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"my-ngram-analyzer\"\n" +
            "      },\n" +
            "      \"kor_TagName\": {\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"my-nori-analyzer\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
}
