package com.pokemonjagt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/api/auth/")
            || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }
}

/*
 * FORKLARING AF JwtAuthenticationFilter
 *
 * OncePerRequestFilter   - Spring-filter der garanterer det kun køres én gang per request
 *                          samme mønster som JWTTokenValidatorFilter i undervisningen
 *
 * doFilterInternal()     - køres på hvert request der ikke er filtreret fra i shouldNotFilter
 *                          1. læser Authorization-headeren
 *                          2. klipper "Bearer " præfikset væk og hiver token'en ud
 *                          3. validerer token med JwtDecoder (henter Supabase's nøgle fra JWKS)
 *                          4. gemmer den validerede bruger i SecurityContextHolder
 *                             (controllers kan derefter læse brugeren med @AuthenticationPrincipal)
 *
 * JwtAuthenticationToken - wrapper der pakker den validerede Jwt ind som et Authentication-objekt
 *                          @AuthenticationPrincipal Jwt jwt i controllers læser herfra
 *
 * shouldNotFilter()      - returnerer true = filteret SPRINGER denne request over
 *                          /api/auth/** = login og register kræver ingen token
 *                          OPTIONS = CORS preflight requests sendes uden token
 */
