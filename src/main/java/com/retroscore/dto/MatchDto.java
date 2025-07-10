package com.retroscore.dto;

import lombok.Data;

import java.time.LocalDate;


@Data
public class MatchDto {

    private Long matchId;
    private String matchTitle;
    private FootballClubDto homeTeam;
    private FootballClubDto awayTeam;
    private String stadiumName;
//    private Integer homeScore;
//    private Integer awayScore;
    private LocalDate matchDate;
    private Integer homeCorners;
    private Integer awayCorners;
    private Integer homeYellowCards;
    private Integer awayYellowCards;
    private Integer homeRedCards;
    private Integer awayRedCards;

}
