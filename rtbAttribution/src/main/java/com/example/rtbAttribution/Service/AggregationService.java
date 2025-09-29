package com.example.rtbAttribution.Service;

import com.example.rtbAttribution.Model.*;
import com.example.rtbAttribution.Config.RtbAggProperties;
import com.example.rtbAttribution.entity.BannerFields;
import com.example.rtbAttribution.entity.BannerVideoFields;
import com.example.rtbAttribution.entity.CampaignFields;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AggregationService {
    private final RtbAggProperties properties;
    private final ElasticsearchService esService;
    private final DatabaseService dbService;

    private final Map<RecordKey, CountFields> aggEvents = new ConcurrentHashMap<>();  // Mirrors OutputCounts
    private final Set<String> activeIntervals = ConcurrentHashMap.newKeySet();  // Tracks active intervals (simplified channels)

    private static final String INTERVAL_STR = "5m";
    private static final long INTERVAL_SECS = 300L;
    private static final Pattern DOMAIN_PATTERN = Pattern.compile("/site_domain=(.*?)/");

    /**
     * Add a postback event to aggregation (mirrors Go's addEvents).
     */
    public void addEvent(PostbackEventFields field) {
        long nowMs = Instant.now().toEpochMilli();
        String tsStr = getIntervalTimestamp(nowMs);
        long tsMs = (nowMs / 1000 / INTERVAL_SECS) * INTERVAL_SECS * 1000;
        Instant intervalTm = Instant.ofEpochMilli(tsMs);


        String domain = extractDomain(field.getPayload());

        RecordKey key = new RecordKey(
                field.getCampaignId(),
                field.getCreativeId(),
                field.getExchange(),
                INTERVAL_STR,
                tsStr,
                domain,
                field.getEventType()
        );


        aggEvents.computeIfAbsent(key, k -> new CountFields(0, 0, 0, false, tsMs, intervalTm));

        // Increment count (thread-safe)
        CountFields count = aggEvents.get(key);
        synchronized (count.getLock()) {
            count.incrementCount();
        }

        log.debug("Added event for key: {}, count now: {}", key, count.getCount());


        if (activeIntervals.add(tsStr)) {
            log.info("New interval started: {}", tsStr);
        }
        asyncEsQuery(field, key);
    }

    /**
     * Async ES attribution query (mirrors esIntervalQueries and searchElastic).
     */
    @Async
    public void asyncEsQuery(PostbackEventFields field, RecordKey key) {
        String searchType = "";
        String searchVal = "";
        if (field.getBidId() != null && !field.getBidId().isEmpty()) {
            searchType = "bid_id";
            searchVal = field.getBidId();
            log.debug("Async query for App (bid_id): {}", searchVal);
        } else if (field.getUid() != null && !field.getUid().isEmpty() && !"no-cookie".equals(field.getUid())) {
            searchType = "uid";
            searchVal = field.getUid();
            log.debug("Async query for Web (uid): {}", searchVal);
        } else {
            log.debug("Skipping invalid query for key: {}", key);
            return;
        }

        boolean found = false;
        String foundStatus = "";
        if ("bid_id".equals(searchType)) {
            found = esService.searchClicks(searchVal);
            foundStatus = found ? "ctc" : "not found";
        } else {
            found = esService.searchPixels(searchVal);
            foundStatus = found ? "vtc" : "not found";
        }


        if (found && aggEvents.containsKey(key)) {
            CountFields count = aggEvents.get(key);
            synchronized (count.getLock()) {
                count.setQueryChecked(true);
                if ("ctc".equals(foundStatus)) {
                    count.incrementCtc();
                } else if ("vtc".equals(foundStatus)) {
                    count.incrementVtc();
                }
            }
            log.info("Attribution found for key {}: {}", key, foundStatus);
        } else {
            log.debug("No attribution for {}: {}", searchType, searchVal);
        }
    }

    /**
     * Periodic flush of expired intervals (mirrors writeLastInterval and ticker).
     */
    @Scheduled(fixedDelay = 300000)  // 5 minutes (300,000 ms)
    public void flushInterval() {
        long nowMs = Instant.now().toEpochMilli();
        long currentIntervalMs = (nowMs / 1000 / INTERVAL_SECS) * INTERVAL_SECS * 1000;


        Set<RecordKey> keysToFlush = new HashSet<>();
        for (Map.Entry<RecordKey, CountFields> entry : aggEvents.entrySet()) {
            RecordKey key = entry.getKey();
            CountFields fields = entry.getValue();
            if (fields.getIntervalTs() <= currentIntervalMs) {
                keysToFlush.add(key);
            }
        }

        if (!keysToFlush.isEmpty()) {
            log.info("Flushing {} events for expired intervals", keysToFlush.size());
            writeToElastic(keysToFlush);
        }


        activeIntervals.removeIf(interval -> {

            try {
                Instant intervalInstant = Instant.parse(interval);
                return intervalInstant.isBefore(Instant.ofEpochMilli(currentIntervalMs));
            } catch (Exception e) {
                return true;
            }
        });


        dbService.refreshData();

        log.info("Flushed interval. Unprocessed events: {}", aggEvents.size());
    }

    /**
     * Flush all remaining on shutdown (mirrors Ctrl+C handling).
     */
    public void flushAllAggregations() {
        Set<RecordKey> allKeys = new HashSet<>(aggEvents.keySet());
        log.warn("Shutdown flush: {} remaining events", allKeys.size());
        writeToElastic(allKeys);
        aggEvents.clear();
        activeIntervals.clear();
        log.info("All aggregations flushed");
    }

    /**
     * Write aggregates to ES (mirrors writeElastic).
     */
    private void writeToElastic(Set<RecordKey> keys) {
        if (properties.isDisableAggSend()) {
            log.info("ES send disabled; clearing {} keys without write", keys.size());
            for (RecordKey key : keys) {
                aggEvents.remove(key);
            }
            return;
        }

        String indexDay = getDateStampedIndex();
        for (RecordKey key : keys) {
            CountFields fields = aggEvents.get(key);
            if (fields == null || fields.getCount() == 0) {
                continue;
            }

            synchronized (fields.getLock()) {

                CampaignFields campaignRec = dbService.findCampaignById(key.getCampaignId());
                if (campaignRec == null) {
                    log.warn("Campaign not found for key: {}", key);
                    continue;
                }

                String adType = "";
                int extCreativeId = 0;
                boolean found = false;


                BannerFields bannerRec = dbService.findBannerById(key.getCampaignId(), key.getCreativeId());
                if (bannerRec != null) {
                    adType = "banner";
                    extCreativeId = bannerRec.getExtCreativeId() != null ? bannerRec.getExtCreativeId() : 0;
                    found = true;
                }


                if (!found) {
                    BannerVideoFields videoRec = dbService.findVideoById(key.getCampaignId(), key.getCreativeId());
                    if (videoRec != null) {
                        adType = "video";
                        extCreativeId = videoRec.getExtCreativeId()!=null ? videoRec.getExtCreativeId() : 0 ;
                        found = true;
                    } else {
                        adType = "unknown";
                    }
                }


                if (!found && "video".equals(adType)) {
                    bannerRec = dbService.findBannerById(key.getCampaignId(), key.getCreativeId());
                    if (bannerRec != null) {
                        adType = "banner";
                        extCreativeId = bannerRec.getExtCreativeId()!=null ? bannerRec.getExtCreativeId() : 0 ;
                    }
                }

                Instant intervalTime = fields.getCount() > 0 ? fields.getIntervalTm() : Instant.now();
                Instant now = Instant.now();


                AggCounter aggRec = new AggCounter();
                aggRec.setCampaignId(key.getCampaignId());
                aggRec.setCreativeId(key.getCreativeId());
                aggRec.setAdType(adType);
                aggRec.setExchange(key.getExchange());
                aggRec.setExtCreativeId(extCreativeId);
                aggRec.setExtLineId(campaignRec.getExtLineId() != null ? campaignRec.getExtLineId() : 0);
                aggRec.setInterval(key.getIntervalStr());
                aggRec.setMediaPlanId(campaignRec.getMediaPlanId() != null ? campaignRec.getMediaPlanId() : 0);
                aggRec.setRegion(campaignRec.getRegions() != null ? campaignRec.getRegions() : " ");
                aggRec.setClientId(campaignRec.getClientId() != null ? campaignRec.getClientId() : 0);
                aggRec.setTimestamp(intervalTime);
                aggRec.setDbTimestamp(now);
                aggRec.setDomain(key.getDomain());
                aggRec.setTotalEvents(fields.getCount());
                aggRec.setFoundEventsCtc(fields.getFoundCtc());
                aggRec.setFoundEventsVtc(fields.getFoundVtc());
                aggRec.setEventType(key.getEventType());


                String docId = String.join("_", String.valueOf(key.getCampaignId()), String.valueOf(key.getCreativeId()), key.getEventType(),
                        key.getExchange(), key.getIntervalStr(), key.getIntervalTs(), key.getDomain()) +
                        "_" + System.nanoTime();

                aggRec.setId(docId);


                try {
                    esService.indexDocument(aggRec, indexDay);
                    log.info("Indexed agg record {} to index {}", docId, indexDay);
                } catch (Exception e) {
                    log.error("Failed to index {}: {}", docId, e.getMessage());
                }


                aggEvents.remove(key);
            }
        }
    }

    /**
     * Extract domain from payload (mirrors Go regex).
     */
    private String extractDomain(String payload) {
        if (payload == null || payload.isEmpty()) {
            return "";
        }
        Matcher matcher = DOMAIN_PATTERN.matcher(payload);
        return matcher.find() ? matcher.group(1) : "";
    }

    /**
     * Compute interval timestamp (mirrors intervalTimestamp).
     */
    private String getIntervalTimestamp(long timestampMs) {
        long epochSecs = timestampMs / 1000;
        long roundedSecs = (epochSecs / INTERVAL_SECS) * INTERVAL_SECS;
        Instant instant = Instant.ofEpochSecond(roundedSecs);
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
        return zdt.format(java.time.format.DateTimeFormatter.ISO_INSTANT);  // RFC3339 equivalent
    }

    /**
     * Generate date-stamped index name (mirrors Go's fmt.Sprintf).
     */
    private String getDateStampedIndex() {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
        return String.format("%s-%d.%02d.%02d",
                properties.getEsAggIndex(),
                now.getYear(),
                now.getMonthValue(),
                now.getDayOfMonth());
    }

    /**
     * Get unprocessed count (for logging).
     */
    public int getUnprocessedCount() {
        return aggEvents.size();
    }
}
