package com.example.task1.splash;

public final class SplashUiState {

    public enum Type { LOADING, READY, ERROR }
    public enum Destination { LOGIN, DASHBOARD }
    public enum ErrorReason { NO_INTERNET, TIME_UNVERIFIED, SERVER_BUSY, UNKNOWN }

    public final Type type;
    public final Destination destination;   // only set when READY
    public final ErrorReason reason;        // only set when ERROR

    private SplashUiState(Type type, Destination destination, ErrorReason reason) {
        this.type = type;
        this.destination = destination;
        this.reason = reason;
    }

    public static SplashUiState loading() { return new SplashUiState(Type.LOADING, null, null); }
    public static SplashUiState ready(Destination d) { return new SplashUiState(Type.READY, d, null); }
    public static SplashUiState error(ErrorReason r) { return new SplashUiState(Type.ERROR, null, r); }
}
