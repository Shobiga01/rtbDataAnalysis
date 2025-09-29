package com.example.video_events.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class VideoEventDto {

    @JsonProperty("adid")
    private int campaignId;

    @JsonProperty("crid")
    private int creativeId;

    private String domain;
    private String exchange;

    @JsonProperty("vastevent")
    private String eventType;

    private long timestamp;
}
