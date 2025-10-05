package com.retroscore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderBoardEntry {
    private Long userId;
    private String username;
    private Integer totalPoints;
    private Integer gamesPlayed;
    private Double winPercentage;
    private Long rank;
    private String profilePictureURL;
}
