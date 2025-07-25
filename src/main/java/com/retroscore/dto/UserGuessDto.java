package com.retroscore.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserGuessDto {

    @NotNull(message = "matchId is required")
    private Long matchId;

    @NotNull(message = "Home score prediction is required")
    @Min(value = 0, message = "Home score must be non negative")
    private Integer predictedHomeScore;

    @NotNull(message = "Away score is required")
    @Min(value = 0,message = "Away score must be non negative")
    private Integer predictedAwayScore;

    public UserGuessDto(){}

    public UserGuessDto(Long matchId, Integer predictedHomeScore, Integer predictedAwayScore){
        this.matchId = matchId;
        this.predictedHomeScore = predictedHomeScore;
        this.predictedAwayScore = predictedAwayScore;
    }

    @Override
    public String toString(){
        return "UserGuessDto{"+
                "matchId="  +matchId+
                "predictedHomeScore="+ predictedHomeScore+
                "predictedAwayScore="+ predictedAwayScore+
                "}";
    }


}
