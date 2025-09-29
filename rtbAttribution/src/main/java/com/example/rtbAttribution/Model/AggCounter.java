package com.example.rtbAttribution.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 * Model for aggregated postback events (output to "postbackeventsagg-*").
 * Enriched with MySQL data (campaign/creative details).
 * Matches Go's AggCounter: Keys + counts + metadata.
 */
@Data
@Document(indexName = "#{T(java.time.format.DateTimeFormatter).ofPattern('yyyy.MM.dd').format(T(java.time.Instant).now())}")  // Dynamic daily index (or use "postbackeventsagg-*")
public class AggCounter {

    @Id  // Generated doc ID (e.g., campaign+creative+...+nano)
    private String id;

    @JsonProperty("campaign_id")
    @Field(type = FieldType.Keyword)
    private Integer campaignId;

    @JsonProperty("creative_id")
    @Field(type = FieldType.Keyword)
    private Integer creativeId;

    @Field(type = FieldType.Keyword)
    private String adType;

    @Field(type = FieldType.Keyword)
    private String exchange;

    @JsonProperty("ext_creative_id")
    @Field(type = FieldType.Keyword)
    private Integer extCreativeId;

    @JsonProperty("ext_line_id")
    @Field(type = FieldType.Keyword)
    private Integer extLineId;

    @Field(type = FieldType.Keyword)
    private String interval;  // "5m"

    @JsonProperty("media_plan_id")
    @Field(type = FieldType.Keyword)
    private Integer mediaPlanId;

    @Field(type = FieldType.Keyword)
    private String region;

    @Field(type = FieldType.Keyword)
    private Integer clientId;

    @JsonProperty("db_timestamp")
    @Field(type = FieldType.Date)
    private Instant dbTimestamp;

    @JsonProperty("timestamp")
    @Field(type = FieldType.Date)
    private Instant timestamp;

    @Field(type = FieldType.Keyword)
    private String domain;

    @Field(type = FieldType.Long)
    @JsonProperty("total_events")
    private long totalEvents;

    @Field(type = FieldType.Long)
    @JsonProperty("found_events_ctc")
    private long foundEventsCtc;

    @Field(type = FieldType.Long)
    @JsonProperty("found_events_vtc")
    private long foundEventsVtc;

    @Field(type = FieldType.Keyword)
    @JsonProperty("postback_event")
    private String eventType;


    public AggCounter() {}



}
