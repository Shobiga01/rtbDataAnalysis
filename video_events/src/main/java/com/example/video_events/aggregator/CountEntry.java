package com.example.video_events.aggregator;

import lombok.Getter;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class CountEntry {
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicInteger count = new AtomicInteger(0);
    private final long intervalTs;
    private final Instant intervalTm;

    public CountEntry(long intervalTs, Instant intervalTm) {
        this.intervalTs = intervalTs;
        this.intervalTm = intervalTm;
    }

    public void increment() {
        count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }
}
