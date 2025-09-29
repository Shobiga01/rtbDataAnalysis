package com.example.rtbAttribution.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;



@Data
@Entity
@Table(name = "banners")
public class BannerFields {
    @Id
    private Integer id;

    @Column(name = "campaign_id", nullable = true)
    private Integer campaignId;

    @Column(name = "ext_creative_id", nullable = true)
    private Integer extCreativeId;

    @Column(name = "width_height_list", nullable = true)
    private String widthHeightList;

    @Column(name = "width", nullable = true)
    private Integer width;

    @Column(name = "height", nullable = true)
    private Integer height;

    @Column(name = "width_range", nullable = true)
    private String widthRange;

    @Column(name = "height_range", nullable = true)
    private String heightRange;
}
