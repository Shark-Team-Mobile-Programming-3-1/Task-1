package com.example.task1.auth;

import android.content.Context;

public class SessionStore {

    private final Context appContext;

    public SessionStore(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** First SharedPreferences read touches the disk: call from a background thread. */
    public boolean isLoggedIn() {
        return appContext.getSharedPreferences("session", Context.MODE_PRIVATE)
                .getBoolean("logged_in", false);
    }
}
