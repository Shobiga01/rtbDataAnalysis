package com.example.Service;

import com.example.Model.MediaPlan;
import com.example.Repository.MediaPlanRepository;
import com.example.Store.InMemoryStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class MediaPlanService {

    private final MediaPlanRepository mediaPlanRepository;

    public MediaPlanService(MediaPlanRepository mediaPlanRepository) {
        this.mediaPlanRepository = mediaPlanRepository;
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        try {
            mediaPlanRepository.findAll()
                    .forEach(mp -> InMemoryStore.mediaPlanMap.put(mp.getId(), mp));
            log.info("Loaded {} media plans into memory", InMemoryStore.mediaPlanMap.size());
        } catch (Exception e) {
            log.error("Error loading media plans from DB", e);
        }
    }


    public Map<BigInteger, MediaPlan> getMediaPlanMap() {
        return InMemoryStore.mediaPlanMap;
    }

    public void updateMediaPlanInMemory(MediaPlan mp) {
        // Changed to use BigInteger comparison to match model
        if (mp.getBids() != null && mp.getBids().compareTo(BigInteger.ZERO) > 0) {
            InMemoryStore.mediaPlanMap.put(mp.getId(), mp);
            log.debug("Updated in-memory media plan: {}", mp.getId());
        } else {
            log.warn("Skipped updating media plan {} due to zero/null bids", mp.getId());
        }
    }

    @Transactional
    public void persistMediaPlans() {
        InMemoryStore.mediaPlanMap.values().stream()
                // Changed to use BigInteger comparison to match model
                .filter(mp -> mp.getBids() != null && mp.getBids().compareTo(BigInteger.ZERO) > 0)
                .forEach(mp -> {
                    try {
                        mediaPlanRepository.save(mp);
                        log.info("Persisted media plan {}", mp.getId());
                    } catch (Exception e) {
                        log.error("Failed to persist media plan {}", mp.getId(), e);
                    }
                });
    }
}