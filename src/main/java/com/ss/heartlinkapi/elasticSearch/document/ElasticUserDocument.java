package com.ss.heartlinkapi.elasticSearch.document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Document(indexName = "user_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticUserDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String documentId;
    @Field(type = FieldType.Long)
    private Long userId;
    @Field(type = FieldType.Keyword)
    private String loginId;
    @Field(type = FieldType.Text)
    private String name;
}
