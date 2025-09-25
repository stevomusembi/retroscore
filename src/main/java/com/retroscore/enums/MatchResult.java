package com.retroscore.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;

public enum MatchResult {
    HOME_WIN,
    AWAY_WIN,
    DRAW;

    @JsonCreator
    public static MatchResult fromString(String value) {
        if (value == null) return null;
        return MatchResult.valueOf(value.trim().toUpperCase());
    }
}
