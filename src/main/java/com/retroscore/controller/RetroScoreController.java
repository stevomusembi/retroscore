package com.retroscore.controller;


import com.retroscore.dto.MatchDto;
import com.retroscore.dto.UserGameResponse;
import com.retroscore.dto.UserGuessDto;
import com.retroscore.entity.Match;
import com.retroscore.entity.User;
import com.retroscore.entity.UserGame;
import com.retroscore.service.AuthService;
import com.retroscore.service.DataImportService;
import com.retroscore.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.PublicKey;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/")
public class RetroScoreController {

    private AuthService authService;
    private GameService gameService;
    private DataImportService dataImportService;

    public RetroScoreController(AuthService authService, GameService gameService, DataImportService dataImportService){
        this.authService = authService;
        this.gameService = gameService;
        this.dataImportService = dataImportService;
    }

    @PostMapping("register")
    public ResponseEntity<User> register(@RequestBody User user){
        authService.register(user);
        return ResponseEntity.ok().body(user);
    }

    @PostMapping("login")
    public ResponseEntity<User> login(@RequestBody User user){
        authService.login(user);
        return ResponseEntity.ok().body(authService.login(user));
    }

    // TODO add filters for random game by season and club

    @GetMapping("game/random-match")
    public ResponseEntity<MatchDto> getRandomMatch(@RequestParam(required = false) Long teamId){
        MatchDto match = gameService.getRandomMatch(teamId);
        return ResponseEntity.ok().body(match);
    }

    @PostMapping("game/guess")
    public ResponseEntity<UserGameResponse> submitGuess(@RequestParam(required = true) Long userId, @RequestBody UserGuessDto userGuess) {
        UserGameResponse response = gameService.submitGuess(userId, userGuess);
        return ResponseEntity.ok(response);
    }

    @GetMapping("user/stats")
    public ResponseEntity<?> getUserStats(User user){
        Long userId = user.getId();
        return ResponseEntity.ok().body(gameService.getUserStats(userId));
    }


}
