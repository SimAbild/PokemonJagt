package com.pokemonjagt.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Handles all communication with the Supabase Authentication API.
 *
 * This is the only place in the backend that knows about Supabase Auth URLs.
 * It acts as a thin proxy: receives credentials from our controller,
 * forwards them to Supabase, and returns Supabase's response as-is.
 *
 * On successful login, Supabase returns an access_token (JWT) which the
 * frontend will store and send with every future request.
 */
@Service
public class AuthService {

    @Value("${supabase.url}")
    private String supabaseProjectUrl;

    // The anon key is required in every Supabase API request header
    // to identify which project is being called
    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    @Value("${supabase.service-role-key}")
    private String supabaseServiceRoleKey;

    // Spring's HTTP client for calling the Supabase REST API
    private final RestTemplate httpClient = new RestTemplate();

    /**
     * Creates a new user account in Supabase.
     *
     * Supabase stores the email and a bcrypt hash of the password.
     * The response contains the new user's details but not a session token —
     * the user must log in separately to receive their JWT.
     */
    public Map<String, Object> registerNewUser(String email, String password) {
        String supabaseSignupEndpoint = supabaseProjectUrl + "/auth/v1/signup";

        HttpEntity<Map<String, String>> requestWithHeaders = new HttpEntity<>(
                Map.of("email", email, "password", password),
                buildSupabaseAuthHeaders()
        );

        ResponseEntity<Map> supabaseResponse = httpClient.exchange(
                supabaseSignupEndpoint, HttpMethod.POST, requestWithHeaders, Map.class
        );

        return supabaseResponse.getBody();
    }

    /**
     * Authenticates a user against Supabase and returns a JWT access token.
     *
     * The "grant_type=password" parameter tells Supabase this is
     * an email/password login (as opposed to OAuth or refresh token).
     *
     * The returned map includes:
     *   - access_token  → the JWT the frontend will use for all future requests
     *   - refresh_token → used to get a new access_token when this one expires
     *   - user          → details about the logged-in user
     */
    public Map<String, Object> loginAndReceiveJwt(String email, String password) {
        String supabaseTokenEndpoint = supabaseProjectUrl + "/auth/v1/token?grant_type=password";

        HttpEntity<Map<String, String>> requestWithHeaders = new HttpEntity<>(
                Map.of("email", email, "password", password),
                buildSupabaseAuthHeaders()
        );

        ResponseEntity<Map> supabaseResponse = httpClient.exchange(
                supabaseTokenEndpoint, HttpMethod.POST, requestWithHeaders, Map.class
        );

        return supabaseResponse.getBody();
    }

    /**
     * Opretter en bruger i Supabase via admin API — kræver service-role nøgle.
     * Bruges kun af gym leader til at oprette nye brugere.
     * Returnerer det UUID som Supabase tildeler den nye bruger.
     */
    public String createUserAsAdmin(String email, String password) {
        String adminEndpoint = supabaseProjectUrl + "/auth/v1/admin/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseServiceRoleKey);
        headers.setBearerAuth(supabaseServiceRoleKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(
                Map.of("email", email, "password", password, "email_confirm", true),
                headers
        );

        ResponseEntity<Map> response = httpClient.exchange(adminEndpoint, HttpMethod.POST, request, Map.class);
        return (String) response.getBody().get("id");
    }

    /**
     * Builds the HTTP headers required for every Supabase API request.
     * The "apikey" header is how Supabase knows which project is being accessed.
     */
    private HttpHeaders buildSupabaseAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);
        return headers;
    }
}
