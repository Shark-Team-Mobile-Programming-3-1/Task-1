package com.example.task1.util;

import com.example.task1.data.SplashRepository;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class TimeValidator {

    private static final String[] NTP_HOSTS = {"time.google.com", "pool.ntp.org"};

    /** NTP (Google) -> NTP (pool) -> HTTPS "Date" header from our own API. */
    public long clockOffsetMs() throws TimeUnverifiedException {
        for (String host : NTP_HOSTS) {
            try {
                return SntpClient.fetchOffsetMs(host, 3_000);
            } catch (IOException ignored) {
                // try the next source
            }
        }
        try {
            return httpsDateOffset();
        } catch (IOException e) {
            throw new TimeUnverifiedException(e);
        }
    }

    private long httpsDateOffset() throws IOException {
        HttpsURLConnection conn =
                (HttpsURLConnection) new URL(SplashRepository.BASE_URL).openConnection();
        try {
            conn.setRequestMethod("HEAD");
            conn.setConnectTimeout(4_000);
            conn.setReadTimeout(4_000);
            long before = System.currentTimeMillis();
            conn.connect();
            long serverDate = conn.getDate();
            if (serverDate == 0L) throw new IOException("No Date header");
            return serverDate - (before + System.currentTimeMillis()) / 2;
        } finally {
            conn.disconnect();
        }
    }
}
