package com.retroscore.enums;


import lombok.Getter;

@Getter
public enum TimerDurations {

    TEN_SECONDS(10, "10 seconds"),
    FIFTEEN_SECONDS(15, "15 seconds"),
    TWENTY_SECONDS(20, "20 seconds"),
    TWENTY_FIVE_SECONDS(25, "25 seconds"),
    THIRTY_SECONDS(30, "30 seconds"),
    FORTY_FIVE_SECONDS(45, "45 seconds");

    private final int seconds;
    private final String displayName;

    TimerDurations(int seconds, String displayName) {
        this.seconds = seconds;
        this.displayName = displayName;
    }

    public static TimerDurations getDefault() {
        return THIRTY_SECONDS;
    }
}
