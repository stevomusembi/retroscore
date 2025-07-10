package com.retroscore.dto;

import com.retroscore.enums.GameResult;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserGameResponse {
    private Long userGameId;
    private Long matchId;
    private String matchTitle;
    private Integer predictedHomeScore;
    private Integer predictedAwayScore;
    private Integer actualHomeScore;
    private Integer actualAwayScore;
    private Boolean isCorrectScore;
    private Boolean isCorrectResult;
    private LocalDateTime playedAt;
    private GameResult gameResult;
    private String resultMessage;

    public UserGameResponse() {}

    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private final UserGameResponse response = new UserGameResponse();

        public Builder userGameId(Long userGameId) {
            response.userGameId = userGameId;
            return this;
        }

        public Builder matchId(Long matchId) {
            response.matchId = matchId;
            return this;
        }

        public Builder matchTitle(String matchTitle) {
            response.matchTitle = matchTitle;
            return this;
        }

        public Builder predictedHomeScore(Integer predictedHomeScore) {
            response.predictedHomeScore = predictedHomeScore;
            return this;
        }

        public Builder predictedAwayScore(Integer predictedAwayScore) {
            response.predictedAwayScore = predictedAwayScore;
            return this;
        }

        public Builder actualHomeScore(Integer actualHomeScore) {
            response.actualHomeScore = actualHomeScore;
            return this;
        }

        public Builder actualAwayScore(Integer actualAwayScore) {
            response.actualAwayScore = actualAwayScore;
            return this;
        }

        public Builder isCorrectScore(Boolean isCorrectScore) {
            response.isCorrectScore = isCorrectScore;
            return this;
        }

        public Builder isCorrectResult(Boolean isCorrectResult) {
            response.isCorrectResult = isCorrectResult;
            return this;
        }

        public Builder gameResult(GameResult gameResult) {
            response.gameResult = gameResult;
            return this;
        }

        public Builder playedAt(LocalDateTime playedAt) {
            response.playedAt = playedAt;
            return this;
        }

        public Builder resultMessage(String resultMessage) {
            response.resultMessage = resultMessage;
            return this;
        }

        public UserGameResponse build() {
            return response;
        }
    }
}
