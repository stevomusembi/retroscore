package com.retroscore.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "matches")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id", nullable = false)
    private FootballClub homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id", nullable = false)
    private FootballClub awayTeam;

    @Column(name = "match_date")
    private LocalDate matchDate;

    @Column(name = "home_score", nullable = false)
    private Integer homeScore;

    @Column(name = "away_score", nullable = false)
    private Integer awayScore;

    @Column(name = "halftime_home_score")
    private Integer halftimeHomeScore;

    @Column(name = "halftime_away_score")
    private Integer halftimeAwayScore;

    @Column(name = "home_corners")
    private Integer homeCorners;

    @Column(name = "away_corners")
    private Integer awayCorners;

    @Column(name = "home_yellow_cards")
    private Integer homeYellowCards;

    @Column(name = "away_yellow_cards")
    private Integer awayYellowCards;

    @Column(name = "home_red_cards")
    private Integer homeRedCards;

    @Column(name = "away_red_cards")
    private Integer awayRedCards;

    @Column(name = "game_week")
    private Integer gameWeek;

    public String getScoreString(){
        return homeScore + "-" + awayScore;
    }

    public String getMatchTitle(){
        return homeTeam.getName() + "vs" + awayTeam.getName();
    }
}
