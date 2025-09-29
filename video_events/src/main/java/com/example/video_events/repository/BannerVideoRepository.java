package com.example.video_events.repository;

import com.example.video_events.model.BannerVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerVideoRepository extends JpaRepository<BannerVideo, Integer> {
    List<BannerVideo> findByCampaignId(Integer campaignId);
}
