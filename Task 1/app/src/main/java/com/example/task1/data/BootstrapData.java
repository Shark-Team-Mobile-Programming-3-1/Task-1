package com.example.task1.data;

import java.util.List;

public class BootstrapData {
    public final String authToken;
    public final List<Integer> examYears;
    public final Announcement announcement;
    public final long clockOffsetMs;        // trusted time - device time
    public final boolean resultsReleased;   // calculated with TRUSTED time

    public BootstrapData(String authToken, List<Integer> examYears, Announcement announcement,
                         long clockOffsetMs, boolean resultsReleased) {
        this.authToken = authToken;
        this.examYears = examYears;
        this.announcement = announcement;
        this.clockOffsetMs = clockOffsetMs;
        this.resultsReleased = resultsReleased;
    }
}
