package com.example.task1.splash;

import java.io.IOException;

public class NoInternetException extends IOException {
    public NoInternetException() {
        super("No internet connection");
    }
}
