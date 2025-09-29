package com.example.Store;

import com.example.Model.Campaign;
import com.example.Model.MediaPlan;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryStore {

    public static final Map<Integer, Campaign> campaignMap = new HashMap<>();


    public static final Map<BigInteger, MediaPlan> mediaPlanMap = new HashMap<>();
}