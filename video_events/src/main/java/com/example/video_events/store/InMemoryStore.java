package com.example.video_events.store;

import com.example.video_events.model.Campaign;
import com.example.video_events.model.BannerVideo;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryStore {

    public final ConcurrentMap<Integer, Campaign> campaigns = new ConcurrentHashMap<>();
    public final ConcurrentMap<Integer, BannerVideo> bannerVideos = new ConcurrentHashMap<>();
}
