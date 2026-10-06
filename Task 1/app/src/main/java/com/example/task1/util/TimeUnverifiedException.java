package com.example.task1.util;

import java.io.IOException;

public class TimeUnverifiedException extends IOException {
    public TimeUnverifiedException(Throwable cause) {
        super("Cannot verify time", cause);
    }
}
