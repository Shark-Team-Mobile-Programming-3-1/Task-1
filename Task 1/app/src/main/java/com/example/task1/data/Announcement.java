package com.example.task1.data;

public class Announcement {
    public final String title;
    public final String message;
    public final long releaseAtEpochMs;   // official release time (UTC millis)

    public Announcement(String title, String message, long releaseAtEpochMs) {
        this.title = title;
        this.message = message;
        this.releaseAtEpochMs = releaseAtEpochMs;
    }
}
