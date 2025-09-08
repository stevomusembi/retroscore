package com.retroscore.controller;

import com.retroscore.dto.UserSettingsDto;
import com.retroscore.security.UserPrincipal;
import com.retroscore.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping
    public ResponseEntity<UserSettingsDto> getUSerSettings(@AuthenticationPrincipal UserPrincipal principal){
        try {
            Long userId = principal.getUserId();
            UserSettingsDto userSettings = userService.getUserSettings(userId);
            return ResponseEntity.ok(userSettings);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    @PutMapping
    public  ResponseEntity<UserSettingsDto> updateUserSettings(@AuthenticationPrincipal UserPrincipal principal,
                                                               @RequestBody UserSettingsDto settingsDto){
        try {
           Long userId = principal.getUserId();
           UserSettingsDto updatedSettings = userService.updateUserSettings(userId, settingsDto);
           return  ResponseEntity.ok(updatedSettings);
        } catch(EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        } catch(Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/difficulty")
    public ResponseEntity<Void> updateGameDifficulty(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestParam String difficulty){
        try {
            Long userId = principal.getUserId();
            userService.updateGameDifficulty(userId, difficulty);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/notifications")
    public ResponseEntity<Void> updateNotifications(@AuthenticationPrincipal UserPrincipal principal,
                                                     @RequestParam Boolean enabled ){
        try {
            Long userId = principal.getUserId();
            userService.updateNotification(userId, enabled);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/preferredLeague")
    public ResponseEntity<Void> updatePreferredLeague(@AuthenticationPrincipal UserPrincipal principal,
                                                    @RequestParam String leagueId ){
        try {
            Long userId = principal.getUserId();
            userService.updatePreferredLeague(userId, leagueId);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }


    @PatchMapping("/hint")
    public ResponseEntity<Void> updateHints(@AuthenticationPrincipal UserPrincipal principal,
                                                    @RequestParam Boolean enabled ){
        try {
            Long userId = principal.getUserId();
            userService.updateHints(userId, enabled);
            return ResponseEntity.ok().build();
        } catch (EntityNotFoundException e){
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }




}
