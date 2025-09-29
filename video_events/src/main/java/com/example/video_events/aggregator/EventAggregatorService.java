package com.example.video_events.aggregator;

import com.example.video_events.model.VideoEventDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class EventAggregatorService {


    private final ConcurrentHashMap<String, CountEntry> aggEvents = new ConcurrentHashMap<>();

    private static final long INTERVAL_SECONDS = 300;
    private static final String INTERVAL_STR = "5m";

    public void addEvent(VideoEventDto ev) {

        IntervalInfo info = computeInterval(ev.getTimestamp(), INTERVAL_SECONDS);
        String key = String.join("_",
                Long.toString(ev.getCampaignId()),
                Long.toString(ev.getCreativeId()),
                ev.getExchange(),
                INTERVAL_STR,
                info.intervalString,
                ev.getEventType());


        aggEvents.compute(key, (k, existing) -> {
            if (existing == null) {
                CountEntry entry = new CountEntry(info.epochMs, info.intervalTm);
                entry.increment();
                return entry;
            } else {
                existing.increment();
                return existing;
            }
        });


        if (log.isDebugEnabled()) {
            log.debug("Added event to key={} count={}", key, aggEvents.get(key).getCount());
        }
    }

    public Map<String, CountEntry> snapshotKeysOlderThan(long tsMs, boolean sendAll) {
        Map<String, CountEntry> result = new HashMap<>();
        for (Map.Entry<String, CountEntry> e : aggEvents.entrySet()) {
            CountEntry ce = e.getValue();
            if (sendAll || ce.getIntervalTs() <= tsMs) {
                result.put(e.getKey(), ce);
            }
        }
        return result;
    }

    public void removeKeys(Collection<String> keys) {
        for (String k : keys) {
            aggEvents.remove(k);
        }
    }

    public void lockKey(String key) {
        CountEntry ce = aggEvents.get(key);
        if (ce != null) ce.getLock().lock();
    }

    public void unlockKey(String key) {
        CountEntry ce = aggEvents.get(key);
        if (ce != null) ce.getLock().unlock();
    }

    public void debugPrint() {
        aggEvents.forEach((k,v) -> log.info("Key {} intervalTs {} count {}", k, v.getIntervalTs(), v.getCount()));
    }

    private static class IntervalInfo {
        String intervalString;
        long epochMs;
        Instant intervalTm;
        IntervalInfo(String s, long ms, Instant tm) { this.intervalString = s; this.epochMs = ms; this.intervalTm = tm; }
    }

    private IntervalInfo computeInterval(long timestampMs, long intervalSeconds) {

        long tsSecs = timestampMs / 1000;
        long rounded = (tsSecs / intervalSeconds) * intervalSeconds;
        Instant tm = Instant.ofEpochSecond(rounded);
        String iso = tm.toString();
        long epochMs = tm.getEpochSecond() * 1000;
        return new IntervalInfo(iso, epochMs, tm);
    }
}
