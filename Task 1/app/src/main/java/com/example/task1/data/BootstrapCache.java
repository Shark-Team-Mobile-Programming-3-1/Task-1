package com.example.task1.data;

/** In-memory hand-off to the next screen (the token is never saved to disk). */
public final class BootstrapCache {
    public static volatile BootstrapData data;
    private BootstrapCache() {}
}
