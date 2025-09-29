package com.example.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigInteger;

@Entity
@Data
@Table(name = "media_plans")
public class MediaPlan {

    @Id
    private BigInteger id;

    private BigInteger bids;
    private BigInteger wins;
    private BigInteger impressions;
    private BigInteger clicks;
    private Double spend;


}

