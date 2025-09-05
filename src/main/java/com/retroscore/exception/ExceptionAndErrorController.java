package com.retroscore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionAndErrorController {
    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleUnauthorizedRequest(UnauthorizedException ex){
        return ex.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateUsername(ConflictException ex){
        return ex.getMessage();
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMatchNotFound(MatchNotFoundException ex){
        return ex.getMessage();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleUserAlreadyPlayedException(UserAlreadyPlayedException ex){
        return ex.getMessage();
    }

    @ExceptionHandler(NoMatchesFoundException.class)
    public ResponseEntity<String> handleMatchNotFoundWithFilters(NoMatchesFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
