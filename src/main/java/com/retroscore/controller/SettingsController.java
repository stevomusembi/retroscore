package com.retroscore.controller;

import com.retroscore.dto.UserSettingsDto;
import com.retroscore.service.UserService;
import jakarta.persistence.EntityNotFoundException;
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
            UserSettingsDto userSettings = userService.getUserSettings(userId);
            return ResponseEntity.ok(userSettings);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @PutMapping("{userId}")
    public  ResponseEntity<UserSettingsDto> updateUserSettings(@PathVariable Long userId, @RequestBody UserSettingsDto settingsDto){
        try {
           UserSettingsDto updatedSettings = userService.updateUserSettings(userId, settingsDto);
           return  ResponseEntity.ok(updatedSettings);
        } catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        } catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{userId}/difficulty")
    public ResponseEntity<Void> updateGameDifficulty(@PathVariable Long userId,
                                                     @RequestParam String difficulty){
        try {
            userService.updateGameDifficulty(userId, difficulty);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{userId}/notifications")
    public ResponseEntity<Void> updateNotifications(@PathVariable Long userId,
                                                     @RequestParam Boolean enabled ){
        try {
            userService.updateNotification(userId, enabled);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{userId}/preferredLeague")
    public ResponseEntity<Void> updatePreferredLeague(@PathVariable Long userId,
                                                    @RequestParam String leagueId ){
        try {
            userService.updatePreferredLeague(userId, leagueId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }


    @PatchMapping("/{userId}/hint")
    public ResponseEntity<Void> updateHints(@PathVariable Long userId,
                                                    @RequestParam Boolean enabled ){
        try {
            userService.updateHints(userId, enabled);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }




}
