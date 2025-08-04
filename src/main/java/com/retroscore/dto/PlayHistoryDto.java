package com.retroscore.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlayHistoryDto{
    private Boolean previousPlayed;
    private String  previousGuess;
    private Boolean wasCorrectResult;
    private Boolean wasCorrectScore;
    private LocalDateTime playedAt;
}
