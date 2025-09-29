package com.example.rtbAttribution.Model;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

/**
 * Model for ES "pixels-*" documents (raw pixel/impression events).
 * Used for VTC attribution: Search by uid to check if view occurred.
 * Matches Go's PixelFields: uid, bid_id, timestamp, payload.
 */
@Data
@Document(indexName = "pixels-*")
public class PixelFields {

    @Id
    private String id;

    @JsonProperty("uid")
    @Field(type = FieldType.Keyword)
    private String uid;

    @JsonProperty("bid_id")
    @Field(type = FieldType.Keyword)
    private String bidId;

    @JsonProperty("timestamp")
    @Field(type = FieldType.Date)
    private Instant timestamp;

    @JsonProperty("payload")
    @Field(type = FieldType.Text)
    private String payload;


    public PixelFields() {}

    public PixelFields(String uid, String bidId, Instant timestamp, String payload) {
        this.uid = uid;
        this.bidId = bidId;
        this.timestamp = timestamp;
        this.payload = payload;
    }


    public static PixelFields fromLongTimestamp(String uid, String bidId, long timestampMs, String payload) {
        return new PixelFields(uid, bidId, Instant.ofEpochMilli(timestampMs), payload);
    }
}

