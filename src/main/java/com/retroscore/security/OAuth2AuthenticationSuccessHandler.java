package com.retroscore.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retroscore.entity.User;
import com.retroscore.repository.UserRepository;
import com.retroscore.service.JWTService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    // Web app frontend URL (for fallback redirects)
    @Value("${app.web.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        logger.info("OAuth2 authentication successful for user: {}", authentication.getName());

        try {
            // Extract UserPrincipal from authentication
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            String email = oidcUser.getEmail();
            String name = oidcUser.getFullName();

            // Load your User entity from DB
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            logger.info("Generating JWT for user ID: {} ({})", user.getId(), user.getEmail());

            // Generate JWT token
            String jwtToken = jwtService.generateToken(user);

            // This handler is only used for web popup authentication
            // Mobile apps use the direct API endpoints (/auth/google/mobile)
            handlePopupSuccess(response, jwtToken, user);

        } catch (Exception e) {
            logger.error("Error during authentication success handling", e);
            handlePopupError(response, "Authentication processing failed");
        }
    }

    /**
     * Handle popup success for web app
     */
    private void handlePopupSuccess(HttpServletResponse response, String jwtToken, User user) throws IOException {
        logger.info("Handling popup success for user: {}", user.getEmail());

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        // Create user data object
        Map<String, Object> userData = new HashMap<>();
        userData.put("accessToken", jwtToken);
        userData.put("email", user.getEmail());
        userData.put("username", user.getUsername() != null ? user.getUsername() : "");
        userData.put("id", user.getId());

        try {
            String userDataJson = objectMapper.writeValueAsString(userData);

            // HTML that communicates success back to parent window
            String html = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Login Success</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            display: flex;
                            justify-content: center;
                            align-items: center;
                            height: 100vh;
                            margin: 0;
                            background-color: #f5f5f5;
                        }
                        .message {
                            text-align: center;
                            padding: 20px;
                            background: white;
                            border-radius: 8px;
                            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        }
                        .loading {
                            color: #666;
                        }
                    </style>
                </head>
                <body>
                    <div class="message">
                        <h2>Login Successful!</h2>
                        <p class="loading">Redirecting you back to the app...</p>
                    </div>
                    <script>
                        console.log('Popup authentication successful');
                        
                        try {
                            // Send success data to parent window
                            if (window.opener && !window.opener.closed) {
                                console.log('Sending message to parent window');
                                window.opener.postMessage({
                                    type: 'GOOGLE_LOGIN_SUCCESS',
                                    data: %s
                                }, '*');
                                
                                // Close popup after short delay
                                setTimeout(() => {
                                    window.close();
                                }, 1000);
                            } else {
                                console.log('No parent window found, redirecting to web app');
                                // Fallback: redirect to your web app with token
                                window.location.href = '%s/login?token=' + encodeURIComponent('%s');
                            }
                        } catch (error) {
                            console.error('Error communicating with parent:', error);
                            // Fallback: show success message and manual close instruction
                            document.querySelector('.loading').innerHTML = 
                                'Success! Please close this window and return to the app.';
                        }
                    </script>
                </body>
                </html>
                """, userDataJson, frontendUrl, jwtToken);

            response.getWriter().write(html);

        } catch (Exception e) {
            logger.error("Error creating popup success response", e);
            throw new IOException("Failed to create success response", e);
        }
    }

    /**
     * Handle popup error for web app
     */
    private void handlePopupError(HttpServletResponse response, String errorMessage) throws IOException {
        logger.error("Handling popup error: {}", errorMessage);

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <title>Login Error</title>
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        background-color: #f5f5f5;
                    }
                    .message {
                        text-align: center;
                        padding: 20px;
                        background: white;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                    }
                    .error { color: #d32f2f; }
                </style>
            </head>
            <body>
                <div class="message">
                    <h2 class="error">Login Failed</h2>
                    <p>%s</p>
                    <p><small>You can close this window and try again.</small></p>
                </div>
                <script>
                    try {
                        if (window.opener && !window.opener.closed) {
                            window.opener.postMessage({
                                type: 'GOOGLE_LOGIN_ERROR',
                                message: '%s'
                            }, '*');
                            setTimeout(() => {
                                window.close();
                            }, 2000);
                        }
                    } catch (error) {
                        console.error('Error communicating with parent:', error);
                    }
                </script>
            </body>
            </html>
            """, errorMessage, errorMessage);

        response.getWriter().write(html);
    }
}