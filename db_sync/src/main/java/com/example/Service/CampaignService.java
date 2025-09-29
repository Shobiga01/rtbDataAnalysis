package com.example.Service;

import com.example.Model.Campaign;
import com.example.Repository.CampaignRepository;
import com.example.Store.InMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
        loadFromDatabase();
    }


    private void loadFromDatabase() {
        try {
            List<Campaign> campaigns = campaignRepository.findAll();
            campaigns.forEach(c -> InMemoryStore.campaignMap.put(c.getId(), c));
            log.info("Loaded {} campaigns into memory", campaigns.size());
        } catch (Exception e) {
            log.error("Error loading campaigns from DB", e);
        }
    }

    public Map<Integer, Campaign> getCampaignMap() {
        return InMemoryStore.campaignMap;
    }

    public void updateCampaignInMemory(Campaign c) {
        if (c.getBids() != null && c.getBids().compareTo(BigInteger.ZERO) > 0) {
            InMemoryStore.campaignMap.put(c.getId(), c);
            log.debug("Updated in-memory campaign: {}", c.getId());
        } else {
            log.warn("Skipped updating campaign {} due to zero/null bids", c.getId());
        }
    }

    public void persistCampaigns() {
        InMemoryStore.campaignMap.values().stream()
                .filter(c -> c.getBids() != null && c.getBids().compareTo(BigInteger.ZERO) > 0)
                .forEach(c -> {
                    try {
                        campaignRepository.save(c);
                        log.info("Persisted campaign {}", c.getId());
                    } catch (Exception e) {
                        log.error("Failed to persist campaign {}", c.getId(), e);
                    }
                });
    }
}