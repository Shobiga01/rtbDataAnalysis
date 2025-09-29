package com.example.video_events.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "banner_videos")
@Getter @Setter @NoArgsConstructor
public class BannerVideo {
    @Id
    private int id;

    @Column(name = "campaign_id")
    private int campaignId;

    @Column(name = "vast_video_width")
    private int width;

    @Column(name = "vast_video_height")
    private int height;

    @Column(name = "ext_creative_id")
    private int  extCreativeId;
}
