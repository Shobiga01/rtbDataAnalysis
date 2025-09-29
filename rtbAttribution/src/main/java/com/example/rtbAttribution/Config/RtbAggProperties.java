package com.example.rtbAttribution.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rtbagg")
public class RtbAggProperties {
    private String brokerList = "kafka:9092";
    private String partition = "0";
    private int offsetType = -1;
    private int messageCountStart = 0;
    private String esRegionUrl = "http://192.168.2.102:9200";
    private String esAggUrl = "http://192.168.2.102:9200";
    private String esAggIndex = "postbackeventsagg";
    private boolean debug = false;
    private boolean disableAggSend = false;
    private long intervalSecs = 300L;  // 5 min
}
