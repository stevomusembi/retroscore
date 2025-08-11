package com.retroscore.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {

    private Long userId;

    private String username;

    private String email;

    private Integer matchesPlayed;

    private Integer matchesPredictedCorrectScore;

    private Integer matchesPredictedWrongScore;

    private Integer totalPoints;

    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    private Double winPercentage;

}
