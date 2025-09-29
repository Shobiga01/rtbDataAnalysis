package com.example.Controller;

import com.example.Model.MediaPlan;
import com.example.Service.MediaPlanService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.Map;

@RestController
@RequestMapping("/api/media-plans")
public class MediaPlanController {

    private final MediaPlanService mediaPlanService;

    public MediaPlanController(MediaPlanService mediaPlanService) {
        this.mediaPlanService = mediaPlanService;
    }

    @GetMapping
    public Map<BigInteger, MediaPlan> getMediaPlans() {
        return mediaPlanService.getMediaPlanMap();
    }

    @PutMapping
    public void updateMediaPlan(@RequestBody MediaPlan mediaPlan) {
        mediaPlanService.updateMediaPlanInMemory(mediaPlan);
    }

    @PostMapping("/persist")
    public void persistMediaPlans() {
        mediaPlanService.persistMediaPlans();
    }
}

