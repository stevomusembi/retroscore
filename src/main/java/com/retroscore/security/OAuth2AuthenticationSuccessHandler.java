package com.retroscore.security;

import com.retroscore.entity.User;
import com.retroscore.service.JWTService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private JWTService jwtService;

    // This should match your Expo app's deep link scheme
    // Configure this in application.properties: app.oauth2.redirect-uri=myapp://auth/success
    @Value("${app.oauth2.redirect-uri:myapp://auth/success}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        logger.info("OAuth2 authentication successful for user: {}", authentication.getName());

        try {
            // Extract UserPrincipal from authentication
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            logger.info("Generating JWT for user ID: {} ({})", user.getId(), user.getEmail());

            // Generate JWT token
            String jwtToken = jwtService.generateToken(user);

            // Create redirect URL with token
            String targetUrl = buildTargetUrl(jwtToken, user);

            logger.info("Redirecting user to: {}", targetUrl);

            // Redirect to Expo app with token
            response.sendRedirect(targetUrl);

        } catch (Exception e) {
            logger.error("Error during authentication success handling", e);

            // If something goes wrong, redirect to error
            String errorUrl = buildErrorUrl("Authentication processing failed");
            response.sendRedirect(errorUrl);
        }
    }

    /**
     * Builds the success redirect URL with JWT token
     */
    private String buildTargetUrl(String token, User user) {
        try {
            // URL encode the token and user info for safety
            String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
            String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
            String encodedUsername = URLEncoder.encode(user.getUsername() != null ? user.getUsername() : "", StandardCharsets.UTF_8);

            // Build URL: myapp://auth/success?token=xyz&email=user@example.com&username=john
            return String.format("%s?token=%s&email=%s&username=%s",
                    redirectUri, encodedToken, encodedEmail, encodedUsername);

        } catch (Exception e) {
            logger.error("Error building target URL", e);
            // Fallback to simple redirect
            return redirectUri + "?token=" + token;
        }
    }

    /**
     * Builds error redirect URL
     */
    private String buildErrorUrl(String errorMessage) {
        try {
            String encodedMessage = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
            return redirectUri.replace("/success", "/error") + "?error=" + encodedMessage;
        } catch (Exception e) {
            logger.error("Error building error URL", e);
            return redirectUri.replace("/success", "/error") + "?error=unknown";
        }
    }
}