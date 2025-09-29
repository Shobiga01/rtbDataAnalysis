package com.example.rtbAttribution.Model;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecordKey implements Serializable {
    private Integer campaignId;
    private Integer creativeId;
    private String exchange;
    private String intervalStr = "5m";
    private String intervalTs;
    private String domain;
    private String eventType;
}
