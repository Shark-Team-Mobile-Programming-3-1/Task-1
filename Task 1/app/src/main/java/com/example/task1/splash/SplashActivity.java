package com.example.task1.splash;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.example.task1.R;
import com.example.task1.auth.DashboardActivity;
import com.example.task1.auth.LoginActivity;

public class SplashActivity extends AppCompatActivity {

    private SplashViewModel viewModel;
    private View logo;
    private ProgressBar progress;
    private TextView status;
    private Button retry;
    private boolean navigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MUST be called before super.onCreate()
        SplashScreen splash = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Fade the system splash icon into our branded screen
        splash.setOnExitAnimationListener(provider ->
                provider.getView().animate()
                        .alpha(0f)
                        .setDuration(200)
                        .withEndAction(provider::remove)
                        .start());

        logo = findViewById(R.id.logo);
        progress = findViewById(R.id.progress);
        status = findViewById(R.id.status);
        retry = findViewById(R.id.retry);

        viewModel = new ViewModelProvider(this).get(SplashViewModel.class);
        retry.setOnClickListener(v -> viewModel.load());

        // LiveData is lifecycle-aware: no updates while the app is in the background
        viewModel.getState().observe(this, this::render);
    }

    private void render(SplashUiState state) {
        switch (state.type) {
            case LOADING:
                progress.setVisibility(View.VISIBLE);
                retry.setVisibility(View.GONE);
                status.setText(R.string.loading_status);
                break;

            case ERROR:
                progress.setVisibility(View.GONE);
                retry.setVisibility(View.VISIBLE);
                status.setText(messageFor(state.reason));
                break;

            case READY:
                navigate(state.destination);
                break;
        }
    }

    private int messageFor(SplashUiState.ErrorReason reason) {
        switch (reason) {
            case NO_INTERNET:     return R.string.err_no_internet;
            case TIME_UNVERIFIED: return R.string.err_time;
            case SERVER_BUSY:     return R.string.err_server;
            default:              return R.string.err_unknown;
        }
    }

    private void navigate(SplashUiState.Destination destination) {
        if (navigated) return;
        navigated = true;

        Class<?> target = (destination == SplashUiState.Destination.DASHBOARD)
                ? DashboardActivity.class
                : LoginActivity.class;

        // Shared-element transition: the logo moves into the next screen
        ActivityOptions options =
                ActivityOptions.makeSceneTransitionAnimation(this, logo, "logo_transition");
        startActivity(new Intent(this, target), options.toBundle());
        finishAfterTransition();
    }
}
