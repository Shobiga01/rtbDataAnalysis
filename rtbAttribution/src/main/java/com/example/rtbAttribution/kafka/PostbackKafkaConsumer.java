package com.example.rtbAttribution.kafka;



import com.example.rtbAttribution.Model.PostbackEventFields;
import com.example.rtbAttribution.Service.AggregationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostbackKafkaConsumer {
    private final AggregationService aggregationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "postbackevents", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<?, ?> record, Acknowledgment ack) {
        try {
            String value = (String) record.value();
            log.debug("Received: topic={}, partition={}, offset={}, value={}", record.topic(), record.partition(), record.offset(), value);

            PostbackEventFields fields = objectMapper.readValue(value, PostbackEventFields.class);
            aggregationService.addEvent(fields);

            ack.acknowledge();
        } catch (Exception e) {
            log.error("Kafka consume failed: {}", e.getMessage());

        }
    }
}

