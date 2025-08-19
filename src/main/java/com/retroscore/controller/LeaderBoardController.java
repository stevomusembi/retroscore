package com.retroscore.controller;

import com.retroscore.dto.LeaderBoardResponse;
import com.retroscore.dto.UserStatsWithRank;
import com.retroscore.service.LeaderBoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/leaderboard")
public class LeaderBoardController {
    @Autowired
    private LeaderBoardService leaderBoardService;

    @GetMapping
    public ResponseEntity<LeaderBoardResponse> getLeaderBoard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        LeaderBoardResponse response = leaderBoardService.getLeaderBoard(page,size);
        return ResponseEntity.ok(response);

    }


    @GetMapping("user/{userId}")
    public ResponseEntity<UserStatsWithRank> getUserStatsWithRank(@PathVariable Long userId){
        UserStatsWithRank userStats = leaderBoardService.getUserStatsWithRank(userId);

        return ResponseEntity.ok(userStats);
    }

}
