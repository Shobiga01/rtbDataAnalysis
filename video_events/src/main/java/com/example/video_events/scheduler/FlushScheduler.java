package com.example.video_events.scheduler;

import com.example.video_events.aggregator.EventAggregatorService;
import com.example.video_events.db.DbReaderService;
import com.example.video_events.es.ElasticWriterService;
import com.example.video_events.aggregator.CountEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class FlushScheduler {

    private final EventAggregatorService aggregator;
    private final DbReaderService dbReader;
    private final ElasticWriterService writer;

    // index prefix like "videoagg"
    @Value("${app.es-agg-video-index}")
    private String indexPrefix;

    public FlushScheduler(EventAggregatorService aggregator,
                          DbReaderService dbReader,
                          ElasticWriterService writer) {
        this.aggregator = aggregator;
        this.dbReader = dbReader;
        this.writer = writer;
    }


    public void init() {
        dbReader.loadAll();
    }


    @Scheduled(fixedDelayString = "${app.flush-interval-ms:300000}")
    public void flushCycle() {
        log.info("Starting flush cycle");
        try {

            long nowMs = System.currentTimeMillis();
            long tsMs = nowMs - 300_000;


            dbReader.loadAll();

            Map<String, CountEntry> keys = aggregator.snapshotKeysOlderThan(tsMs, false);
            if (!keys.isEmpty()) {
                log.info("Found {} keys to flush", keys.size());
                writer.writeKeysToEs(indexPrefix, keys);
            } else {
                log.info("No keys to flush");
            }
        } catch (Exception e) {
            log.error("Error during flush cycle", e);
        }
    }
}
