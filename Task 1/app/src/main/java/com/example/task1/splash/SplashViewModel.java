package com.example.task1.splash;

import android.app.Application;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.task1.auth.SessionStore;
import com.example.task1.data.Announcement;
import com.example.task1.data.BootstrapCache;
import com.example.task1.data.BootstrapData;
import com.example.task1.data.SplashRepository;
import com.example.task1.util.NetworkChecker;
import com.example.task1.util.TimeUnverifiedException;
import com.example.task1.util.TimeValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class SplashViewModel extends AndroidViewModel {

    public static final long MIN_SPLASH_MS = 1_500L;
    public static final long OVERALL_TIMEOUT_MS = 15_000L;

    private final NetworkChecker network;
    private final TimeValidator timeValidator = new TimeValidator();
    private final SplashRepository repo = new SplashRepository();
    private final SessionStore session;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final MutableLiveData<SplashUiState> state =
            new MutableLiveData<>(SplashUiState.loading());
    private final AtomicInteger runCounter = new AtomicInteger();
    private Future<?> job;

    public SplashViewModel(@NonNull Application app) {
        super(app);
        network = new NetworkChecker(app);
        session = new SessionStore(app);
        load();
    }

    public LiveData<SplashUiState> getState() {
        return state;
    }

    /** Called on start and by the Retry button (always on the main thread). */
    public void load() {
        if (job != null) job.cancel(true);
        final int myRun = runCounter.incrementAndGet();
        state.setValue(SplashUiState.loading());
        job = executor.submit(() -> runBootstrap(myRun));
    }

    // ---------- runs on a BACKGROUND thread ----------
    private void runBootstrap(int myRun) {
        final long start = SystemClock.elapsedRealtime();
        final long deadline = start + OVERALL_TIMEOUT_MS;
        final List<Future<?>> tasks = new ArrayList<>();
        SplashUiState outcome;

        try {
            // 1) Fast local internet check: fail fast, no wasted requests
            if (!network.isOnline()) throw new NoInternetException();

            // 2) Everything else runs IN PARALLEL
            Future<Long> offsetF = executor.submit((Callable<Long>) () -> timeValidator.clockOffsetMs());
            Future<String> tokenF = executor.submit((Callable<String>) () -> repo.fetchToken());
            Future<List<Integer>> yearsF = executor.submit((Callable<List<Integer>>) () -> repo.fetchExamYears());
            Future<Announcement> bannerF = executor.submit((Callable<Announcement>) () -> repo.fetchAnnouncement());
            Future<Boolean> loggedInF = executor.submit((Callable<Boolean>) () -> session.isLoggedIn());
            tasks.add(offsetF);
            tasks.add(tokenF);
            tasks.add(yearsF);
            tasks.add(bannerF);
            tasks.add(loggedInF);

            long clockOffset = await(offsetF, deadline);
            Announcement announcement = await(bannerF, deadline);

            // Compare with TRUSTED time, never the phone clock alone
            long trustedNow = System.currentTimeMillis() + clockOffset;
            boolean released = trustedNow >= announcement.releaseAtEpochMs;

            BootstrapData data = new BootstrapData(
                    await(tokenF, deadline),
                    await(yearsF, deadline),
                    announcement,
                    clockOffset,
                    released);
            boolean loggedIn = await(loggedInF, deadline);

            BootstrapCache.data = data;
            outcome = SplashUiState.ready(loggedIn
                    ? SplashUiState.Destination.DASHBOARD
                    : SplashUiState.Destination.LOGIN);

        } catch (InterruptedException e) {          // cancelled by Retry / ViewModel cleared
            Thread.currentThread().interrupt();
            return;
        } catch (TimeoutException e) {
            outcome = SplashUiState.error(SplashUiState.ErrorReason.SERVER_BUSY);
        } catch (NoInternetException e) {
            outcome = SplashUiState.error(SplashUiState.ErrorReason.NO_INTERNET);
        } catch (TimeUnverifiedException e) {
            outcome = SplashUiState.error(SplashUiState.ErrorReason.TIME_UNVERIFIED);
        } catch (IOException e) {
            outcome = SplashUiState.error(SplashUiState.ErrorReason.SERVER_BUSY);
        } catch (Exception e) {
            outcome = SplashUiState.error(SplashUiState.ErrorReason.UNKNOWN);
        } finally {
            for (Future<?> f : tasks) f.cancel(true);   // clean up any unfinished work
        }

        // Keep the branding on screen for at least 1500 ms (success path only)
        if (outcome.type == SplashUiState.Type.READY) {
            long remaining = MIN_SPLASH_MS - (SystemClock.elapsedRealtime() - start);
            if (remaining > 0) {
                try {
                    Thread.sleep(remaining);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        // Ignore results of an old run (e.g. user pressed Retry meanwhile)
        if (myRun == runCounter.get()) state.postValue(outcome);
    }

    /** Waits for a task but never past the overall deadline. */
    private <T> T await(Future<T> future, long deadline) throws Exception {
        long remaining = Math.max(1, deadline - SystemClock.elapsedRealtime());
        try {
            return future.get(remaining, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw e;
        }
    }

    @Override
    protected void onCleared() {
        executor.shutdownNow();
    }
}
