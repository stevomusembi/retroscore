package com.retroscore.controller;

import com.retroscore.dto.UserSettingsDto;
import com.retroscore.entity.User;
import com.retroscore.enums.GameDifficulty;
import com.retroscore.repository.UserRepository;
import com.retroscore.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
@Slf4j
public class SettingsController {

    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserSettingsDto> getUSerSettings(@PathVariable Long userId){
        try {
            User user = userService.findById(userId);
            if(user == null){
                return ResponseEntity.notFound().build();
            }

            log.info("user object {}", user);
            UserSettingsDto settings = new UserSettingsDto(
                    user.isNotificationsEnabled(),
                    user.isMatchReminders(),
                    user.isScoreUpdates(),
                    user.getPreferredLeague(),
                    user.isShowHints(),
                    user.getGameDifficulty(),
                    user.getTimeLimit()
            );

            return ResponseEntity.ok(settings);

        } catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("{userId}")
    public  ResponseEntity<UserSettingsDto> updateUserSettings(@PathVariable Long userId, @RequestBody UserSettingsDto settingsDto){
        try {
            User user = userService.findById(userId);
            if(user == null){
                return ResponseEntity.notFound().build();
            }
            user.setNotificationsEnabled(settingsDto.isNotificationsEnabled());
            user.setMatchReminders(settingsDto.isMatchReminders());
            user.setScoreUpdates(settingsDto.isScoreUpdates());
            user.setPreferredLeague(settingsDto.getPreferredLeague());
            user.setGameDifficulty(settingsDto.getGameDifficulty());
            user.setShowHints(settingsDto.isShowHints());
            user.setTimeLimit(settingsDto.getTimeLimit());

            User updatedUser = userService.updateUser(user);

            UserSettingsDto updatedSettings = new UserSettingsDto(
                    updatedUser.isNotificationsEnabled(),
                    updatedUser.isScoreUpdates(),
                    updatedUser.isMatchReminders(),
                    updatedUser.getPreferredLeague(),
                    updatedUser.isShowHints(),
                    updatedUser.getGameDifficulty(),
                    updatedUser.getTimeLimit()
            );
           return ResponseEntity.ok(updatedSettings);

        } catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{userId}/difficulty")
    public ResponseEntity<Void> updateGameDifficulty(@PathVariable Long userId,
                                                     @RequestParam String difficulty){
        try {
            User user = userService.findById(userId);
            if(user == null){
                return ResponseEntity.notFound().build();
            }

            user.setGameDifficulty(GameDifficulty.valueOf(difficulty.toUpperCase()));
            userService.updateUser(user);
            return ResponseEntity.ok().build();

        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{userId}/notifications")
    public ResponseEntity<Void> updateGameDifficulty(@PathVariable Long userId,
                                                     @RequestParam Boolean enabled ){
        try {
            User user = userService.findById(userId);
            if(user == null){
                return ResponseEntity.notFound().build();
            }

          user.setNotificationsEnabled(enabled);
            userService.updateUser(user);
            return ResponseEntity.ok().build();

        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }






}
