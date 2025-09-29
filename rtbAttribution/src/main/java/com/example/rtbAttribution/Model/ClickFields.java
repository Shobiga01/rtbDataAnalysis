package com.example.rtbAttribution.Model;

import jakarta.persistence.Id;
import lombok.Data;

import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;



@Data
@Document(indexName = "clicks-*")
public class ClickFields {
    @Id
    private String id;
    @Field(type = FieldType.Keyword)
    private String uid;

    @Field(type = FieldType.Keyword)
    private String bidId;

    @Field(type = FieldType.Date)
    private java.time.Instant timestamp;

    @Field(type = FieldType.Text)
    private String payload;

}




