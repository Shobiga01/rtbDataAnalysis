package com.example.rtbAttribution.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "banner_videos")
public class BannerVideoFields {
    @Id
    private Integer id;

    @Column(name = "campaign_id", nullable = true)
    private Integer campaignId;

    @Column(name = "ext_creative_id", nullable = true)
    private Integer extCreativeId;

    @Column(name = "vast_video_width", nullable = true)
    private Integer width;

    @Column(name = "vast_video_height", nullable = true)
    private Integer height;

    @Column(name = "width_height_list", nullable = true)
    private String widthHeightList;
}

