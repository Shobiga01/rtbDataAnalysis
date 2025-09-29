package com.example.Controller;

import com.example.Model.Campaign;
import com.example.Service.CampaignService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping
    public Map<Integer, Campaign> getCampaigns() {
        return campaignService.getCampaignMap();
    }

    @PutMapping
    public void updateCampaign(@RequestBody Campaign campaign) {
        campaignService.updateCampaignInMemory(campaign);
    }

    @PostMapping("/persist")
    public void persistCampaigns() {
        campaignService.persistCampaigns();
    }
}
