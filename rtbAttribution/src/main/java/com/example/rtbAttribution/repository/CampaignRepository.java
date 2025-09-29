package com.example.rtbAttribution.repository;


import com.example.rtbAttribution.entity.CampaignFields;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CampaignRepository extends JpaRepository<CampaignFields, Integer> {
    Optional<CampaignFields> findById(Integer id);
}

