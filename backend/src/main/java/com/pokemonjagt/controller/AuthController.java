package com.pokemonjagt.controller;

import com.pokemonjagt.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Map;

/**
 * Handles user registration and login.
 *
 * These two endpoints are intentionally left open (no JWT required).
 * That is configured in WebConfig — the JwtInterceptor skips /api/auth/**.
 *
 * The login endpoint is where the JWT flow begins:
 * the frontend sends credentials here and receives a JWT in return.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Catches errors that Supabase sends back (e.g. wrong password, email already in use).
     * Without this, RestTemplate would throw an exception that becomes a confusing 500 error.
     * Instead we forward Supabase's original status code so the frontend knows what went wrong.
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handleSupabaseAuthError(HttpClientErrorException supabaseError) {
        return ResponseEntity.status(supabaseError.getStatusCode())
                .body(supabaseError.getResponseBodyAsString());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<String> handleSupabaseServerError(HttpServerErrorException supabaseError) {
        return ResponseEntity.status(supabaseError.getStatusCode())
                .body(supabaseError.getResponseBodyAsString());
    }

    /**
     * POST /api/auth/register
     *
     * Creates a new Supabase user account.
     * After registering, the user must call /login to receive their JWT.
     *
     * Request body:  { "email": "trainer@example.com", "password": "secret" }
     * Response:      Supabase user object (no token yet)
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerNewTrainer(
            @RequestBody Map<String, String> registrationRequest) {

        Map<String, Object> supabaseUserResponse = authService.registerNewUser(
                registrationRequest.get("email"),
                registrationRequest.get("password")
        );

        return ResponseEntity.ok(supabaseUserResponse);
    }

    /**
     * POST /api/auth/login
     *
     * Authenticates the trainer and returns a JWT.
     * The frontend stores this JWT and sends it as a Bearer token on every future request.
     *
     * Request body:  { "email": "ash@pokemon.com", "password": "password123" }
     * Response:      { "access_token": "<JWT>", "token_type": "bearer", "user": {...} }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginTrainer(
            @RequestBody Map<String, String> loginRequest) {

        Map<String, Object> tokenResponse = authService.loginAndReceiveJwt(
                loginRequest.get("email"),
                loginRequest.get("password")
        );

        return ResponseEntity.ok(tokenResponse);
    }
}
