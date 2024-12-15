package com.ss.heartlinkapi.elasticSearch.document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.time.LocalDateTime;

@Document(indexName = "search_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryDocument {
    @Id
    @Field(type = FieldType.Keyword)
    private String documentId;
    @Field(type = FieldType.Long)
    private Long searchHistoryId;
    @Field(type = FieldType.Long)
    private Long userId;
    @Field(type = FieldType.Text)
    private String keyword;
    @Field(type = FieldType.Keyword)
    private String type;
    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;
}
