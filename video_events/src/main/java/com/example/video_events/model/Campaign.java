package com.example.video_events.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "campaigns")
@Getter @Setter @NoArgsConstructor
public class Campaign {
    @Id
    private int id;

    @Column(name = "regions")
    private String regions;

    @Column(name = "ext_line_id")
    private int extLineId;

    @Column(name = "media_plan_id")
    private int mediaPlanId;

    @Column(name = "client_id")
    private int clientId;
}
