package com.example.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigInteger;

@Entity
@Data
@Table(name = "campaigns")
public class Campaign {

    @Id
    private int  id;

    private String status;
    private BigInteger bids;
    private BigInteger wins;
    private BigInteger pixels;
    private BigInteger clicks;
    private Double cost;


}

