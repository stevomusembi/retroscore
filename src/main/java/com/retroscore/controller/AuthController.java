package com.retroscore.controller;

import com.retroscore.entity.User;
import com.retroscore.service.AuthService;
import com.retroscore.service.DataImportService;
import com.retroscore.service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService, GameService gameService, DataImportService dataImportService){
        this.authService = authService;

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
}
