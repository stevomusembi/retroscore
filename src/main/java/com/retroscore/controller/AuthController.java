package com.retroscore.controller;

import com.retroscore.dto.AuthResponse;
import com.retroscore.dto.GoogleTokenRequest;
import com.retroscore.dto.GoogleUserInfo;
import com.retroscore.entity.User;
import com.retroscore.service.AuthService;
import com.retroscore.service.GoogleTokenValidatorService;
import com.retroscore.service.JWTService;
import com.retroscore.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // Allow mobile apps to call these endpoints
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final GoogleTokenValidatorService googleTokenValidator;
    private final JWTService jwtService;
    private final UserService userService;

    @Autowired
    public AuthController(AuthService authService,
                          GoogleTokenValidatorService googleTokenValidator,
                          JWTService jwtService,
                          UserService userService) {
        this.authService = authService;
        this.googleTokenValidator = googleTokenValidator;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    /**
     * Mobile OAuth2 endpoint - for apps using Expo Auth Session
     * This is for iOS/Android apps that handle Google OAuth2 themselves
     */
    @PostMapping("/google/mobile")
    public ResponseEntity<AuthResponse> authenticateGoogleMobile(
            @Valid @RequestBody GoogleTokenRequest request) {

        logger.info("Mobile Google authentication request received");

        try {
            // Validate Google token with Google's servers
            GoogleUserInfo googleUser = googleTokenValidator.validateToken(request.getGoogleToken());

            if (googleUser == null) {
                logger.warn("Invalid Google token provided");
                return ResponseEntity.status(401)
                        .body(new AuthResponse(null, "Invalid Google token", false));
            }

            // Find or create user in our database
            User user = userService.findOrCreateFromGoogleInfo(googleUser);

            // Generate our JWT token
            String jwtToken = jwtService.generateToken(user);

            logger.info("Mobile Google authentication successful for user: {}", user.getEmail());

            // Return success response with JWT
            return ResponseEntity.ok(new AuthResponse(jwtToken, "Authentication successful", true, user));

        } catch (Exception e) {
            logger.error("Error during mobile Google authentication", e);
            return ResponseEntity.status(500)
                    .body(new AuthResponse(null, "Authentication failed: " + e.getMessage(), false));
        }
    }

    /**
     * Token validation endpoint - for verifying JWT tokens
     * Used by both web and mobile clients
     */
    @PostMapping("/validate")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401)
                        .body(new AuthResponse(null, "Missing or invalid authorization header", false));
            }

            String token = authHeader.substring(7);

            if (!jwtService.isTokenExpired(token)) {
                String userEmail = jwtService.getUsernameFromToken(token);
                User user = userService.findByEmail(userEmail);

                return ResponseEntity.ok(new AuthResponse(token, "Token is valid", true, user));
            } else {
                return ResponseEntity.status(401)
                        .body(new AuthResponse(null, "Token is expired or invalid", false));
            }

        } catch (Exception e) {
            logger.error("Error validating token", e);
            return ResponseEntity.status(401)
                    .body(new AuthResponse(null, "Token validation failed", false));
        }
    }

    /**
     * Refresh token endpoint
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401)
                        .body(new AuthResponse(null, "Missing authorization header", false));
            }

            String oldToken = authHeader.substring(7);
            String userEmail = jwtService.getUsernameFromToken(oldToken);

            if (userEmail != null) {
                User user = userService.findByEmail(userEmail);
                String newToken = jwtService.generateToken(user);

                return ResponseEntity.ok(new AuthResponse(newToken, "Token refreshed successfully", true, user));
            } else {
                return ResponseEntity.status(401)
                        .body(new AuthResponse(null, "Invalid token for refresh", false));
            }

        } catch (Exception e) {
            logger.error("Error refreshing token", e);
            return ResponseEntity.status(401)
                    .body(new AuthResponse(null, "Token refresh failed", false));
        }
    }

    /**
     * User profile endpoint - get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).build();
            }

            String token = authHeader.substring(7);
            String userEmail = jwtService.getUsernameFromToken(token);

            if (userEmail != null && !jwtService.isTokenExpired(token)) {
                User user = userService.findByEmail(userEmail);
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.status(401).build();
            }

        } catch (Exception e) {
            logger.error("Error getting current user", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Logout endpoint - invalidate token (optional, since JWT is stateless)
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(@RequestHeader("Authorization") String authHeader) {

        // Since JWT is stateless, logout is mainly client-side (delete token)
        // But you could maintain a blacklist of tokens if needed

        logger.info("User logout requested");
        return ResponseEntity.ok(new AuthResponse(null, "Logged out successfully", true));
    }
}