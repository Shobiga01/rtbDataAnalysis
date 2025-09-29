package com.example.rtbAttribution.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "campaigns")
public class CampaignFields {
    @Id
    private Integer id;

    @Column(name = "regions", nullable = true)
    private String regions;

    @Column(name = "media_plan_id", nullable = true)
    private Integer mediaPlanId;

    @Column(name = "client_id", nullable = true)
    private Integer clientId;

    @Column(name = "ext_line_id", nullable = true)
    private Integer extLineId;
}
