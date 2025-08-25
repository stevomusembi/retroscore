package com.retroscore.service;

import com.retroscore.entity.User;
import com.retroscore.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

@Service
public class JWTService {
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationInMs;

    public JWTService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String generateToken(User user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("username", user.getUsername());
        claims.put("profilePicture",user.getProfilePicture());
        return createToken(claims, user.getId().toString());
    }
    private String createToken(Map<String, Object> claims, String subject){
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+jwtExpirationInMs))
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateRefreshToken(User user){
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");

        return  Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis()+ refreshExpirationInMs))
                .signWith(getSignInKey())
                .compact();
    }


    public Boolean validateToken(String token){
        try{
            Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token);
            return !isTokenExpired(token);
        } catch (MalformedJwtException e){
            System.err.println("Invalid JWT token: "+ e.getMessage());
        } catch (ExpiredJwtException e){
            System.err.println("Jwt token is expired: "+ e.getMessage());
        } catch (UnsupportedJwtException e){
            System.err.println("Jwt token is unsupported :" + e.getMessage());
        } catch (IllegalArgumentException e){
            System.err.println("Jwt claims string is empty: "+ e.getMessage());
        }
        return false;
    }

    public Long getUserIdFromToken(String token){
        String userId = getClaimFromToken(token, Claims::getSubject);
        return Long.parseLong(userId);
    }

    public String getUsernameFromToken(String token){
        return getClaimFromToken(token, claims ->claims.get("username", String.class));
    }

    public String getEmailFromToken(String token){
        return getClaimFromToken(token, claims ->claims.get("email", String.class));
    }

    public Date getExpirationDateFromToken(String token){
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver){
        final Claims claims  = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    public Claims getAllClaimsFromToken(String token){
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    public Boolean isTokenExpired(String token){
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String refreshAccessToken(String refreshToken){
        if(!validateToken(refreshToken)){
            throw new IllegalArgumentException("Invalid refresh token");
        }
        Long userId = getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return generateToken(user);
    }

    public String extractTokenFromHeader(String authHeader){
        if (authHeader!= null && authHeader.startsWith("Bearer")){
            return authHeader.substring(7);
        }
        return  null;
    }


}
