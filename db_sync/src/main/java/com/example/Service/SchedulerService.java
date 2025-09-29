package com.example.Service;

import com.example.Model.Campaign;
import com.example.Model.MediaPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final CampaignService campaignService;
    private final MediaPlanService mediaPlanService;
    private final ElasticAggregationService elasticService;

    @Value("${app.es-agg-index-campaigns}")
    private String esAggIndexCampaigns;

    @Value("${app.es-agg-url}")
    private String esAggUrl;

    @Scheduled(fixedDelayString = "PT5M") // run every 5 minutes
    public void syncData() {
        Instant start = Instant.now();
        log.info("Starting scheduled sync at {}", start);

        try {

            Map<Integer, Campaign> campaigns = campaignService.getCampaignMap();
            if (!campaigns.isEmpty()) {
                elasticService.aggregateCampaigns(esAggIndexCampaigns);
                campaignService.persistCampaigns();
                log.info("Processed {} campaigns", campaigns.size());
            } else {
                log.info("No active campaigns to process");
            }


            Map<BigInteger, MediaPlan> mediaPlans = mediaPlanService.getMediaPlanMap();
            if (!mediaPlans.isEmpty()) {
                elasticService.aggregateMediaPlans(esAggIndexCampaigns);
                mediaPlanService.persistMediaPlans();
                log.info("Processed {} media plans", mediaPlans.size());
            } else {
                log.info("No active media plans to process");
            }

        } catch (Exception e) {
            log.error("Error during scheduled sync", e);
        }

        Duration elapsed = Duration.between(start, Instant.now());
        log.info("DB sync completed in {} seconds", elapsed.toSeconds());
        log.info("----------------------------------------------------");
    }
}
