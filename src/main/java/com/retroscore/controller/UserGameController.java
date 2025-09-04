package com.retroscore.controller;


import com.retroscore.dto.MatchDto;
import com.retroscore.dto.UserDto;
import com.retroscore.dto.UserGameResponse;
import com.retroscore.dto.UserGuessDto;
import com.retroscore.service.AuthService;
import com.retroscore.service.DataImportService;
import com.retroscore.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/")
public class UserGameController {

    private final GameService gameService;
    public UserGameController(AuthService authService, GameService gameService, DataImportService dataImportService){
        this.gameService = gameService;
    }


    @GetMapping("game/random-match")
    public ResponseEntity<MatchDto> getRandomMatch(@RequestParam(required = false) Long userId,
                                                   @RequestParam(required = false) Long teamId,
                                                   @RequestParam(required = false) Long seasonId,
                                                   @RequestParam(required = false, defaultValue="discovery") String mode){
        System.out.println("got here to the request");
        MatchDto match = gameService.getRandomMatch(userId,teamId,seasonId,mode);
        return ResponseEntity.ok().body(match);
    }

    @PostMapping("game/guess")
    public ResponseEntity<UserGameResponse> submitGuess(@RequestParam(required = true) Long userId, @RequestBody UserGuessDto userGuess) {
        UserGameResponse response = gameService.submitGuess(userId, userGuess);
        return ResponseEntity.ok(response);
    }

    @GetMapping("user/stats")
    public ResponseEntity<UserDto> getUserStats(@RequestParam(required = true) Long userId){
        return ResponseEntity.ok().body(gameService.getUserStats(userId));
    }


}
