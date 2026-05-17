package com.pokemonjagt.controller;

import com.pokemonjagt.dto.LoginRequest;
import com.pokemonjagt.dto.RegisterRequest;
import com.pokemonjagt.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

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

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerNewTrainer(@RequestBody RegisterRequest body) {
        Map<String, Object> response = authService.registerNewUser(body.getEmail(), body.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginTrainer(@RequestBody LoginRequest body) {
        Map<String, Object> response = authService.loginAndReceiveJwt(body.getEmail(), body.getPassword());
        return ResponseEntity.ok(response);
    }
}
