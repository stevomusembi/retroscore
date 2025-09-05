package com.retroscore.exception;

public class NoMatchesFoundException extends RuntimeException{
    public NoMatchesFoundException(){
        super("No match found with given filters");
    }
}
