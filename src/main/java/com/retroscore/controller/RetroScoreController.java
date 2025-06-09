package com.retroscore.controller;


import com.retroscore.entity.Match;
import com.retroscore.entity.User;
import com.retroscore.entity.UserGame;
import com.retroscore.service.AuthService;
import com.retroscore.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RetroScoreController {

    private AuthService authService;
    private GameService gameService;

    public RetroScoreController(AuthService authService, GameService gameService){
        this.authService = authService;
        this.gameService = gameService;
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

    // TODO add endpoint for filters for random game by season and club

    @GetMapping("game/random-match")
    public ResponseEntity<Match> getRandomMatch(){
        return ResponseEntity.ok().body(gameService.getRandomMatch());
    }

    @PostMapping("game/guess")
    public ResponseEntity<UserGame> submitGuess(UserGame userGame){
        return ResponseEntity.ok().body(gameService.submitGuess(userGame));
    }

    @GetMapping("user/stats")
    public ResponseEntity<?> getUserStats(User user){
        Long userId = user.getId();
        return ResponseEntity.ok().body(gameService.getUserStats(userId));
    }

//    @GetMapping("user/history")
//    public ResponseEntity<?> getUserHistory(User user){
//        return ResponseEntity.ok().body(gameService.getUserHistory());
//    }

}
