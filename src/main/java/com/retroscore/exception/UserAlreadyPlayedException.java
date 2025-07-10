package com.retroscore.exception;

public class UserAlreadyPlayedException extends RuntimeException {

    public UserAlreadyPlayedException(Long userId, Long matchId){
        super("User "+ userId + "has already played this game " + matchId  + " before ");
    }
}
