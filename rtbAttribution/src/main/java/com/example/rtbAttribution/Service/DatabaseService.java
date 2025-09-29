package com.example.rtbAttribution.Service;

import com.example.rtbAttribution.entity.*;
import com.example.rtbAttribution.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseService {
    private final CampaignRepository campaignRepository;
    private final BannerRepository bannerRepository;
    private final BannerVideoRepository bannerVideoRepository;


    private final ConcurrentHashMap<Integer, CampaignFields> dbCampaignRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, BannerFields> dbBannerRecords = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, BannerVideoFields> dbBannerVideoRecords = new ConcurrentHashMap<>();

    public boolean loadAllData() {
        try {

            List<CampaignFields> campaigns = campaignRepository.findAll();
            campaigns.forEach(c -> dbCampaignRecords.put(c.getId(), c));
            log.info("Loaded {} campaign records", campaigns.size());

            List<BannerFields> banners = bannerRepository.findAll();
            banners.forEach(b -> dbBannerRecords.put(b.getId(), b));
            log.info("Loaded {} banner records", banners.size());


            List<BannerVideoFields> videos = bannerVideoRepository.findAll();
            videos.forEach(v -> dbBannerVideoRecords.put(v.getId(), v));
            log.info("Loaded {} video records", videos.size());

            return true;
        } catch (Exception e) {
            log.error("MySQL load failed", e);
            return false;
        }
    }


    public CampaignFields findCampaignById(int id) {
        return dbCampaignRecords.get(id);
    }


    public BannerFields findBannerById(int campaignId, int creativeId) {

        BannerFields banner = dbBannerRecords.get(creativeId);
        if (banner != null && banner.getCampaignId() == campaignId) {
            return banner;
        }


        return bannerRepository.findByIdAndCampaignId(creativeId, campaignId).orElse(null);
    }


    public BannerVideoFields findVideoById(int campaignId, int creativeId) {

        BannerVideoFields video = dbBannerVideoRecords.get(creativeId);
        if (video != null && video.getCampaignId() == campaignId) {
            return video;
        }


        return bannerVideoRepository.findByIdAndCampaignId(creativeId, campaignId).orElse(null);
    }

    @Scheduled(fixedDelayString = "${rtbagg.interval-secs:300000}")
    public void refreshData() {
        loadAllData();
        log.info("DB data refreshed");
    }
}