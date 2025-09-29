package com.example.rtbAttribution.Model;


import lombok.Data;
import lombok.Synchronized;

import java.time.Instant;

@Data
public class CountFields {
    private final Object lock = new Object();
    private int count = 0;
    private int foundCtc = 0;
    private int foundVtc = 0;
    private boolean queryChecked = false;
    private long intervalTs;  // ms
    private Instant intervalTm;

    @Synchronized
    public void incrementCount() {
        count++;
    }

    @Synchronized
    public void incrementCtc() {
        foundCtc++;
    }

    @Synchronized
    public void incrementVtc() {
        foundVtc++;
    }



    public CountFields(int count, int foundCtc, int foundVtc, boolean queryChecked, long intervalTs, Instant intervalTm) {
        this.count = count;
        this.foundCtc = foundCtc;
        this.foundVtc = foundVtc;
        this.queryChecked = queryChecked;
        this.intervalTs = intervalTs;
        this.intervalTm = intervalTm;
    }

}

