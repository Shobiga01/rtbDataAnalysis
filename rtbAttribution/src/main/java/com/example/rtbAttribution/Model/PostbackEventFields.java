package com.example.rtbAttribution.Model;



import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PostbackEventFields {
    @JsonProperty("bidid")
    private String bidId;

    @JsonProperty("uid")
    private String uid;

    @JsonProperty("campaign_id")
    private Integer campaignId;

    @JsonProperty("creative_id")
    private Integer creativeId;

    private String exchange;
    @JsonProperty("postback_event")
    private String eventType;
    private String payload;


}

