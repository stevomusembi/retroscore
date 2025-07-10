package com.retroscore.entity;

import com.retroscore.enums.GameResult;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_games")
public class UserGame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id",nullable = false)
    private Match match;

    @Column(name = "predicted_home_score")
    private Integer predictedHomeScore;

    @Column(name = "predicted_away_score")
    private Integer predictedAwayScore;

    @Column(name = "is_correct_score" )
    private Boolean isCorrectScore;

    @Column(name = "is_correct_result")
    private Boolean isCorrectResult;

    @Column(name = "played_at")
    private LocalDateTime playedAt;


    public String getPredictedScoreString(){
        return predictedHomeScore + "-" + predictedAwayScore;
    }
    public GameResult getGameResult(){
        if(isCorrectScore) return GameResult.EXACT_SCORE;
        if(isCorrectResult) return GameResult.CORRECT_RESULT;
        return GameResult.INCORRECT;
    }
}
