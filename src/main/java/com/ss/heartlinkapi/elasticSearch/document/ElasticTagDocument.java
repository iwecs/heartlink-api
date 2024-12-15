package com.ss.heartlinkapi.elasticSearch.document;

import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "tag_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ElasticTagDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String documentId;
    @Field(type = FieldType.Long)
    private Long tagId;
    @Field(type = FieldType.Keyword)
    private String tagName;
    @Field(type = FieldType.Text, name = "eng_TagName")
    private String engTagName;
    @Field(type = FieldType.Text, name = "kor_TagName")
    private String korTagName;
}
