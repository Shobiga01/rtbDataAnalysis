package com.example.rtbAttribution.repository;

import com.example.rtbAttribution.entity.BannerVideoFields;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface BannerVideoRepository extends JpaRepository<BannerVideoFields, Long> {
    @Query("SELECT v FROM BannerVideoFields v WHERE v.id = ?1 AND v.campaignId = ?2")
    Optional<BannerVideoFields> findByIdAndCampaignId(Integer id, Integer campaignId);


}

