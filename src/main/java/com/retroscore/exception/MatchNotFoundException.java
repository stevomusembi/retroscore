package com.retroscore.exception;

public class MatchNotFoundException extends RuntimeException {

    public  MatchNotFoundException(Long matchId){
        super("Match with ID, "+ matchId + " not found");
    }
}
