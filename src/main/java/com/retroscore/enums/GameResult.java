package com.retroscore.enums;


import lombok.Getter;

@Getter
public enum GameResult {
    EXACT_SCORE(3, "Perfect 3 points! Exact score !"),
    CORRECT_RESULT(1, "Good 1 point! Correct result"),
    INCORRECT(0, "Wrong guess, try again!");

    private final int points;
    private final String message;

    GameResult(int points, String message){
        this.points = points;
        this.message = message;
    }

}
