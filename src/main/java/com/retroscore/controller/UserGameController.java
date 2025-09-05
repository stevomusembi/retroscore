package com.retroscore.controller;


import com.retroscore.dto.MatchDto;
import com.retroscore.dto.UserDto;
import com.retroscore.dto.UserGameResponse;
import com.retroscore.dto.UserGuessDto;
import com.retroscore.security.UserPrincipal;
import com.retroscore.service.AuthService;
import com.retroscore.service.DataImportService;
import com.retroscore.service.GameService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class UserGameController {

    private final GameService gameService;
    private static final Logger log = LoggerFactory.getLogger(UserGameController.class);

    @GetMapping("game/random-match")
    public ResponseEntity<MatchDto> getRandomMatch(@AuthenticationPrincipal UserPrincipal principal,
                                                   @RequestParam(required = false) Long teamId,
                                                   @RequestParam(required = false) Long seasonId,
                                                   @RequestParam(required = false, defaultValue="discovery") String mode){
        log.info("Received request for random match by user {}", principal.getUserId());
        Long userId = principal.getUserId();
        MatchDto match = gameService.getRandomMatch(userId,teamId,seasonId,mode);
        return ResponseEntity.ok().body(match);
    }

    @PostMapping("game/guess")
    public ResponseEntity<UserGameResponse> submitGuess(@AuthenticationPrincipal UserPrincipal principal,
                                                        @RequestBody UserGuessDto userGuess) {
        Long userId = principal.getUserId();
        UserGameResponse response = gameService.submitGuess(userId, userGuess);
        return ResponseEntity.ok(response);
    }
}
