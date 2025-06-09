package com.retroscore.enums;


import lombok.Getter;

@Getter
public enum GameResult {
    EXACT_SCORE(10, "Perfect ! Exact score !"),
    CORRECT_RESULT(5, "Good! Correct result"),
    INCORRECT(0, "Wrong guess, try again!");

    private final int points;
    private final String message;

    GameResult(int points, String message){
        this.points = points;
        this.message = message;
    }

}
