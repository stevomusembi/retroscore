package com.retroscore.security;
import com.retroscore.entity.User;
import com.retroscore.service.JWTService;
import com.retroscore.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class OptionalJWTAuthenticationFilter extends OncePerRequestFilter{
    private static final Logger logger = LoggerFactory.getLogger(OptionalJWTAuthenticationFilter.class);

    private final JWTService jwtService;
    private final UserService userService;

    @Autowired
    public OptionalJWTAuthenticationFilter(JWTService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);

                // Validate token
                if (!jwtService.isTokenExpired(token)) {
                    String userEmail = jwtService.getUsernameFromToken(token);

                    if (userEmail != null) {
                        User user = userService.findByEmail(userEmail);

                        if (user != null) {
                            // Create authentication token
                            UserPrincipal userPrincipal =  UserPrincipal.create(user);
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            userPrincipal,
                                            null,
                                            userPrincipal.getAuthorities() // Make sure User implements UserDetails or has getAuthorities()
                                    );

                            // Set authentication in security context
                            SecurityContextHolder.getContext().setAuthentication(authentication);

                            logger.debug("JWT authenticated user: {}", user.getEmail());
                        } else {
                            logger.debug("User not found for email: {}", userEmail);
                        }
                    }
                } else {
                    logger.debug("JWT token is expired");
                }
            } catch (Exception e) {
                // Log error but don't block request
                logger.debug("JWT authentication failed: {}", e.getMessage());
                // Clear any partial authentication
                SecurityContextHolder.clearContext();
            }
        }

        // Always continue the filter chain
        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip this filter for OAuth2 endpoints to avoid conflicts
        String path = request.getRequestURI();
        return path.startsWith("/oauth2/") ||
                path.startsWith("/login/oauth2/");
    }
}
