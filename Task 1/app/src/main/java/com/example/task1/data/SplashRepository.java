package com.example.task1.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class SplashRepository {

    public static final String BASE_URL = "https://api.uneb.example.ug/";   // PLACEHOLDER URL
    private static final int MAX_RETRIES = 2;

    public String fetchToken() throws IOException {
        String body = requestWithRetry("v1/auth/anonymous-token", "POST");
        try {
            return new JSONObject(body).getString("token");
        } catch (JSONException e) {
            throw new IOException("Bad token response", e);
        }
    }

    public List<Integer> fetchExamYears() throws IOException {
        String body = requestWithRetry("v1/exams/years", "GET");
        try {
            JSONArray arr = new JSONObject(body).getJSONArray("years");
            List<Integer> years = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) years.add(arr.getInt(i));
            return years;
        } catch (JSONException e) {
            throw new IOException("Bad years response", e);
        }
    }

    public Announcement fetchAnnouncement() throws IOException {
        String body = requestWithRetry("v1/announcement.json", "GET");
        try {
            JSONObject o = new JSONObject(body);
            return new Announcement(o.getString("title"), o.getString("message"), o.getLong("releaseAt"));
        } catch (JSONException e) {
            throw new IOException("Bad announcement response", e);
        }
    }

    // Exponential backoff + jitter so 1.5M phones don't all retry at the same instant
    private String requestWithRetry(String path, String method) throws IOException {
        int attempt = 0;
        while (true) {
            try {
                return request(path, method);
            } catch (IOException e) {
                if (e instanceof InterruptedIOException || attempt++ >= MAX_RETRIES) throw e;
                try {
                    Thread.sleep((300L << attempt) + ThreadLocalRandom.current().nextLong(0, 300));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException("Cancelled");
                }
            }
        }
    }

    private String request(String path, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(BASE_URL + path).openConnection();
        try {
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5_000);     // never wait forever on rural networks
            conn.setReadTimeout(5_000);
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            if (code < 200 || code > 299) throw new IOException("HTTP " + code);

            StringBuilder sb = new StringBuilder();
            try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) sb.append(line);
            }
            return sb.toString();
        } finally {
            conn.disconnect();
        }
    }
}
