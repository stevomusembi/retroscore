package com.retroscore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsWithRank {
    private Long userId;
    private String username;
    private Integer totalPoints;
    private Integer gamesPlayed;
    private Double winPercentage;
    private Integer exactScorePredictions;
    private Integer correctResultPredictions;
    private Long currentRank;
    private String profilePictureURL;
}
