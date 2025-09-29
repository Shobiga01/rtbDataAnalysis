package com.example.rtbAttribution.repository;



import com.example.rtbAttribution.entity.BannerFields;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<BannerFields, Integer> {
    @Query("SELECT b FROM BannerFields b WHERE b.id = ?1 AND b.campaignId = ?2")
    Optional<BannerFields> findByIdAndCampaignId(Integer id, Integer campaignId);



}

