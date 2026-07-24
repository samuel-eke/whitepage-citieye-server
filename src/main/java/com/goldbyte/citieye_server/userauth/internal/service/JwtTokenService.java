package com.goldbyte.citieye_server.userauth.internal.service;

import com.goldbyte.citieye_server.userauth.internal.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;

/**
 * Service that handles creation and parsing of JSON Web Tokens (JWT).
 */
@Service
public class JwtTokenService {

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SecurityConstants.JWT_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates a signed JWT token for the specified username and authorities.
     *
     * @param username    the unique identifier of the user (e.g. email)
     * @param authorities list of roles and permissions granted to the user
     * @return signed JWT token string
     */
    public String generateToken(String username, Collection<String> authorities) {
        return Jwts.builder()
                .issuer("CitiEye Community Governance")
                .subject("JWT Token CitiEye")
                .claim("username", username)
                .claim("authorities", String.join(",", authorities))
                .issuedAt(new Date())
                .expiration(new Date((new Date().getTime() + 86400000))) // 24 hours validity
                .signWith(getSigningKey()).compact();
    }

    /**
     * Parses and validates a JWT token and returns its claims.
     *
     * @param token the JWT token to parse
     * @return the claims contained in the token
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
