package com.example.task1.auth;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.task1.R;
import com.example.task1.data.BootstrapCache;
import com.example.task1.data.BootstrapData;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        BootstrapData data = BootstrapCache.data;
        if (data != null) {
            ((TextView) findViewById(R.id.banner)).setText(data.announcement.message);
        }
    }
}
