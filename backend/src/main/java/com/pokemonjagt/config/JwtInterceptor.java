package com.pokemonjagt.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * Runs before every protected API request.
 *
 * Its job is to check that the caller has a valid Supabase JWT.
 * If the token is valid, it stores the user's ID on the request so controllers
 * can use it without ever touching the token again.
 * If the token is missing or invalid, the request is rejected with 401.
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    // Injected from application.properties — must match the secret Supabase used to sign the token
    @Value("${supabase.jwt-secret}")
    private String supabaseJwtSecret;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String authorizationHeader = request.getHeader("Authorization");

        // Supabase sends tokens in the format: "Bearer <token>"
        // Reject anything that doesn't follow this pattern
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "Authorization header is missing or not in Bearer format");
            return false;
        }

        // Strip the "Bearer " prefix to get the raw JWT string
        String rawJwtToken = authorizationHeader.substring("Bearer ".length());

        try {
            // Validate the token's signature using the Supabase JWT secret.
            // If the signature doesn't match, parseSignedClaims throws an exception.
            Claims verifiedTokenClaims = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(supabaseJwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseSignedClaims(rawJwtToken)
                    .getPayload();

            // The "sub" (subject) claim in a Supabase JWT is the user's UUID from auth.users
            String authenticatedUserId = verifiedTokenClaims.getSubject();

            // Store the user ID on the request so every controller can read it
            // without needing to parse the JWT again
            request.setAttribute("authenticatedUserId", authenticatedUserId);

            return true; // Token is valid — let the request through

        } catch (Exception tokenIsInvalidOrExpired) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT token is invalid or has expired");
            return false;
        }
    }
}
