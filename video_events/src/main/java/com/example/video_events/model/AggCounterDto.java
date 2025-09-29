package com.example.video_events.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
@Builder
public class AggCounterDto {
    private int campaignId;
    private int creativeId;
    private String exchange;
    private int extCreativeId;
    private int extLineId;
    private String interval;
    private int mediaPlanId;
    private String region;
    private int clientId;
    private Instant timestamp;
    private Instant dbTimestamp;
    private int events;
    private String eventType;
}