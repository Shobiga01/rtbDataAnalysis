package com.example.video_events.db;

import com.example.video_events.model.BannerVideo;
import com.example.video_events.model.Campaign;
import com.example.video_events.repository.BannerVideoRepository;
import com.example.video_events.repository.CampaignRepository;
import com.example.video_events.store.InMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DbReaderService {

    private final CampaignRepository campaignRepository;
    private final BannerVideoRepository bannerVideoRepository;
    private final InMemoryStore store;

    public DbReaderService(CampaignRepository campaignRepository,
                           BannerVideoRepository bannerVideoRepository,
                           InMemoryStore store) {
        this.campaignRepository = campaignRepository;
        this.bannerVideoRepository = bannerVideoRepository;
        this.store = store;
    }

    public void loadAll() {
        try {
            List<Campaign> campaigns = campaignRepository.findAll();
            store.campaigns.clear();
            campaigns.forEach(c -> store.campaigns.put(c.getId(), c));
            log.info("Loaded {} campaigns into store", campaigns.size());
        } catch (Exception e) {
            log.error("Failed to load campaigns from DB", e);
        }

        try {
            List<BannerVideo> videos = bannerVideoRepository.findAll();
            store.bannerVideos.clear();
            videos.forEach(v -> store.bannerVideos.put(v.getId(), v));
            log.info("Loaded {} banner videos into store", videos.size());
        } catch (Exception e) {
            log.error("Failed to load banner videos from DB", e);
        }
    }
}
