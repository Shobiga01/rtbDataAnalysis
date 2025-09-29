//package com.example.video_events.config;
//
//
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.elasticsearch.action.bulk.BulkRequest;
//import org.elasticsearch.action.index.IndexRequest;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class VideoEventConsumer {
//
//    private final RestHighLevelClient esClient;
//
//    @Value("${app.es-video-index}")
//    private String esVideoIndex;
//
//    /**
//     * Consume video events from Kafka and store in Elasticsearch
//     */
//    @KafkaListener(topics = "video_events", groupId = "video-events-group")
//    public void consume(String message) {
//        log.info("Received video event: {}", message);
//
//        try {
//            // Example payload -> parse as needed (JSON parsing if structured)
//            Map<String, Object> doc = new HashMap<>();
//            doc.put("raw_event", message);
//            doc.put("timestamp", System.currentTimeMillis());
//
//            BulkRequest bulk = new BulkRequest();
//            bulk.add(new IndexRequest(esVideoIndex).source(doc));
//
//            esClient.bulk(bulk, RequestOptions.DEFAULT);
//            log.info("Video event persisted to Elasticsearch");
//
//        } catch (IOException e) {
//            log.error("Error writing video event to Elasticsearch", e);
//        } catch (Exception ex) {
//            log.error("Unexpected error processing video event", ex);
//        }
//    }
//}
//



package com.example.video_events.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoEventConsumer {

    private final ElasticsearchClient esClient;

    @Value("${app.es-video-index}")
    private String esVideoIndex;

    @KafkaListener(topics = "video_events", groupId = "video-events-group")
    public void consume(String message) {
        log.info("Received video event: {}", message);

        try {
            Map<String, Object> doc = new HashMap<>();
            doc.put("raw_event", message);
            doc.put("timestamp", System.currentTimeMillis());

            IndexRequest<Map<String, Object>> request = IndexRequest.of(i -> i
                    .index(esVideoIndex)
                    .document(doc)
            );

            IndexResponse response = esClient.index(request);
            log.info("Indexed video event id {} to index {}", response.id(), esVideoIndex);

        } catch (Exception ex) {
            log.error("Error indexing video event to Elasticsearch", ex);
        }
    }
}
