package com.retroscore.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler.class);

    // Web app frontend URL (for fallback)
    @Value("${app.web.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        logger.error("OAuth2 authentication failed", exception);

        // Determine the type of error and create appropriate message
        String errorMessage = determineErrorMessage(exception);
        String errorCode = determineErrorCode(exception);

        logger.info("Authentication failed with message: {} (code: {})", errorMessage, errorCode);

        try {
            // This handler is only used for web popup authentication
            // Mobile apps use the direct API endpoints and handle errors there
            handlePopupFailure(response, errorCode, errorMessage);

        } catch (Exception e) {
            logger.error("Error during authentication failure handling", e);
            handlePopupFallback(response, "Authentication failed");
        }
    }

    /**
     * Handle popup failure for web app
     */
    private void handlePopupFailure(HttpServletResponse response, String errorCode, String errorMessage) throws IOException {
        logger.info("Handling popup failure with code: {} and message: {}", errorCode, errorMessage);

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
                        max-width: 400px;
                    }
                    .error { 
                        color: #d32f2f; 
                        margin-bottom: 15px;
                    }
                    .error-code {
                        font-family: monospace;
                        background: #f5f5f5;
                        padding: 5px 10px;
                        border-radius: 4px;
                        font-size: 12px;
                        color: #666;
                        margin-top: 10px;
                    }
                    .close-instruction {
                        margin-top: 15px;
                        color: #666;
                        font-size: 14px;
                    }
                </style>
            </head>
            <body>
                <div class="message">
                    <h2 class="error">Login Failed</h2>
                    <p>%s</p>
                    <div class="error-code">Error Code: %s</div>
                    <p class="close-instruction">You can close this window and try again.</p>
                </div>
                <script>
                    console.log('Popup authentication failed:', {
                        code: '%s',
                        message: '%s'
                    });
                    
                    try {
                        // Send error data to parent window
                        if (window.opener && !window.opener.closed) {
                            console.log('Sending error message to parent window');
                            window.opener.postMessage({
                                type: 'GOOGLE_LOGIN_ERROR',
                                code: '%s',
                                message: '%s'
                            }, '*');
                            
                            // Close popup after delay to allow user to read message
                            setTimeout(() => {
                                window.close();
                            }, 3000);
                        } else {
                            console.log('No parent window found');
                            // If no parent window, redirect to web app login with error
                            setTimeout(() => {
                                window.location.href = '%s/login?error=' + encodeURIComponent('%s');
                            }, 5000);
                        }
                    } catch (error) {
                        console.error('Error communicating with parent:', error);
                    }
                </script>
            </body>
            </html>
            """,
                errorMessage, errorCode,  // For HTML display
                errorCode, errorMessage,  // For console logging
                errorCode, errorMessage,  // For postMessage
                frontendUrl, errorMessage  // For fallback redirect
        );

        response.getWriter().write(html);
    }

    /**
     * Handle popup fallback when other error handling fails
     */
    private void handlePopupFallback(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String html = String.format("""
            <!DOCTYPE html>
            <html>
            <head><title>Authentication Error</title></head>
            <body>
                <h2>Authentication Failed</h2>
                <p>%s</p>
                <p><small>Please close this window and try again.</small></p>
                <script>
                    try {
                        if (window.opener && !window.opener.closed) {
                            window.opener.postMessage({
                                type: 'GOOGLE_LOGIN_ERROR',
                                message: '%s'
                            }, '*');
                            setTimeout(() => window.close(), 2000);
                        }
                    } catch (e) {
                        console.error('Communication error:', e);
                    }
                </script>
            </body>
            </html>
            """, message, message);

        response.getWriter().write(html);
    }

    /**
     * Determines user-friendly error message based on exception type
     */
    private String determineErrorMessage(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            String errorCode = oauth2Exception.getError().getErrorCode();

            return switch (errorCode) {
                case "invalid_request" -> "Invalid authentication request";
                case "unauthorized_client" -> "App is not authorized for Google login";
                case "access_denied" -> "You denied access to your Google account";
                case "unsupported_response_type" -> "Authentication configuration error";
                case "invalid_scope" -> "Invalid permission scope requested";
                case "server_error" -> "Google authentication server error";
                case "temporarily_unavailable" -> "Google authentication temporarily unavailable";
                default -> "Google authentication failed";
            };
        }

        // Generic authentication errors
        String exceptionMessage = exception.getMessage();
        if (exceptionMessage != null && exceptionMessage.toLowerCase().contains("email")) {
            return "Email verification required";
        } else if (exceptionMessage != null && exceptionMessage.toLowerCase().contains("network")) {
            return "Network connection error";
        } else if (exceptionMessage != null && exceptionMessage.toLowerCase().contains("timeout")) {
            return "Authentication request timed out";
        }

        return "Authentication failed. Please try again.";
    }

    /**
     * Determines error code for client-side handling
     */
    private String determineErrorCode(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oauth2Exception) {
            return oauth2Exception.getError().getErrorCode();
        }

        // Map common exceptions to codes
        String exceptionClass = exception.getClass().getSimpleName();
        return switch (exceptionClass) {
            case "BadCredentialsException" -> "invalid_credentials";
            case "AccountExpiredException" -> "account_expired";
            case "LockedException" -> "account_locked";
            case "DisabledException" -> "account_disabled";
            default -> "authentication_error";
        };
    }
}