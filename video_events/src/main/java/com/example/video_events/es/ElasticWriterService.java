package com.example.video_events.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.IndexResponse;
import com.example.video_events.aggregator.CountEntry;
import com.example.video_events.aggregator.EventAggregatorService;
import com.example.video_events.model.AggCounterDto;
import com.example.video_events.model.BannerVideo;
import com.example.video_events.model.Campaign;
import com.example.video_events.store.InMemoryStore;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticWriterService {

    private final ElasticsearchClient esClient;
    private final InMemoryStore store;
    private final EventAggregatorService aggregator;
    private final ObjectMapper objectMapper;

    public void writeKeysToEs(String indexPrefix, Map<String, CountEntry> keysToWrite) {
        if (keysToWrite == null || keysToWrite.isEmpty()) return;

        for (Map.Entry<String, CountEntry> e : keysToWrite.entrySet()) {
            String key = e.getKey();
            CountEntry entry = e.getValue();

            entry.getLock().lock();
            try {
                String[] parts = key.split("_");
                if (parts.length < 6) {
                    aggregator.removeKeys(Set.of(key));
                    continue;
                }


                int campaignId = (int) Long.parseLong(parts[0]);
                int creativeId = (int) Long.parseLong(parts[1]);
                String exchange = parts[2];
                String intervalStr = parts[3];
                String eventType = String.join("_", Arrays.copyOfRange(parts, 5, parts.length));

                Campaign campaign = store.campaigns.get(campaignId);
                BannerVideo video = store.bannerVideos.get(creativeId);

                int extCreativeId = 0;
                if (video != null ) {
                    extCreativeId = video.getExtCreativeId();
                }

                int extLineId = campaign != null ? campaign.getExtLineId() : 0;
                int mediaPlanId = campaign != null ? campaign.getMediaPlanId() : 0;
                String region = campaign != null && campaign.getRegions() != null ? campaign.getRegions() : "";
                int clientId = campaign != null ? campaign.getClientId() : 0;

                Instant intervalTime = entry.getIntervalTm();
                Instant now = Instant.now();

                AggCounterDto dto = AggCounterDto.builder()
                        .campaignId(campaignId)
                        .creativeId(creativeId)
                        .exchange(exchange)
                        .extCreativeId(extCreativeId)
                        .extLineId(extLineId)
                        .interval(intervalStr)
                        .mediaPlanId(mediaPlanId)
                        .region(region)
                        .clientId(clientId)
                        .timestamp(intervalTime)
                        .dbTimestamp(now)
                        .events(entry.getCount())
                        .eventType(eventType)
                        .build();

                String indexName = buildDailyIndexName(indexPrefix, now);
                String docId = buildDocId(parts, eventType, now);

                IndexRequest<AggCounterDto> request = IndexRequest.of(i -> i
                        .index(indexName)
                        .id(docId)
                        .document(dto)
                );

                IndexResponse response = esClient.index(request);
                log.info("Indexed agg record {} to index {}.", docId, indexName);

                aggregator.removeKeys(Set.of(key));

            } catch (Exception ex) {
                log.error("Failed to index record key {}", key, ex);
            } finally {
                entry.getLock().unlock();
            }
        }
    }

    private String buildDailyIndexName(String prefix, Instant now) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd").withZone(ZoneOffset.UTC);
        return String.format("%s-%s", prefix, fmt.format(now));
    }

    private String buildDocId(String[] keyParts, String eventType, Instant now) {
        String prefix = String.join("_", Arrays.copyOfRange(keyParts, 0, Math.min(5, keyParts.length)));
        return prefix + "_" + eventType + "_" + now.getNano();
    }
}