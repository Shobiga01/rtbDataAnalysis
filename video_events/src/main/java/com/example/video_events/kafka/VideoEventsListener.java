package com.example.video_events.kafka;

import com.example.video_events.aggregator.EventAggregatorService;
import com.example.video_events.model.VideoEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VideoEventsListener {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final EventAggregatorService aggregator;

    public VideoEventsListener(EventAggregatorService aggregator) {
        this.aggregator = aggregator;
    }

    @KafkaListener(topics = "videoevents", groupId = "rtb-videoagg-consumer-group")
    public void listen(String message) {
        try {
            VideoEventDto dto = objectMapper.readValue(message, VideoEventDto.class);
            aggregator.addEvent(dto);

            if (log.isDebugEnabled()) {
                log.debug("Consumed video event: {}", dto);
            }
        } catch (Exception e) {
            log.error("Failed to parse/handle Kafka message: {}", message, e);
        }
    }
}
