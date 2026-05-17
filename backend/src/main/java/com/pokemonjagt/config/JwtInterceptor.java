package com.pokemonjagt.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.math.BigInteger;
import java.net.URI;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Runs before every protected API request.
 *
 * Its job is to check that the caller has a valid Supabase JWT.
 * If the token is valid, it stores the user's ID on the request so controllers
 * can use it without ever touching the token again.
 * If the token is missing or invalid, the request is rejected with 401.
 *
 * HOW JWT SIGNING WORKS HERE:
 * Supabase signs JWTs with a private ECC (P-256) key it holds secretly.
 * We verify those tokens using the matching PUBLIC key, which Supabase
 * publishes openly at its JWKS endpoint. We never see the private key.
 * This is safer than the old shared-secret approach (HS256).
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Value("${supabase.url}")
    private String supabaseUrl;

    // Jackson is used to parse the JSON response from the JWKS endpoint
    private final ObjectMapper jsonParser = new ObjectMapper();

    // The public key is fetched once on the first request and cached for reuse.
    // It only changes when Supabase rotates its signing key (rare).
    private PublicKey cachedSupabasePublicKey;
    private final Object keyFetchLock = new Object();

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
            PublicKey supabasePublicKey = getOrFetchSupabasePublicKey();

            // Parse and verify the JWT using Supabase's public key.
            // If the signature was not created by Supabase's private key, this throws.
            Claims verifiedTokenClaims = Jwts.parser()
                    .verifyWith(supabasePublicKey)
                    .build()
                    .parseSignedClaims(rawJwtToken)
                    .getPayload();

            // The "sub" (subject) claim in a Supabase JWT is the user's UUID from auth.users
            String authenticatedUserId = verifiedTokenClaims.getSubject();

            // Store the user ID on the request so every controller can read it
            // without needing to re-parse the JWT
            request.setAttribute("authenticatedUserId", authenticatedUserId);

            return true; // Token is valid — let the request through

        } catch (Exception tokenIsInvalidOrExpired) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT token is invalid or has expired");
            return false;
        }
    }

    /**
     * Returns the cached Supabase public key, fetching it from the JWKS endpoint on first use.
     *
     * Uses double-checked locking so only one thread fetches the key
     * even if multiple requests arrive simultaneously before the cache is populated.
     */
    private PublicKey getOrFetchSupabasePublicKey() throws Exception {
        if (cachedSupabasePublicKey != null) return cachedSupabasePublicKey;

        synchronized (keyFetchLock) {
            if (cachedSupabasePublicKey != null) return cachedSupabasePublicKey;
            cachedSupabasePublicKey = fetchEccPublicKeyFromSupabaseJwks();
        }

        return cachedSupabasePublicKey;
    }

    /**
     * Fetches the ECC public key from Supabase's JWKS (JSON Web Key Set) endpoint.
     *
     * JWKS is a standard format for publishing public keys so that other services
     * can verify JWTs without needing the private key.
     *
     * Supabase's JWKS contains the x and y coordinates of the ECC P-256 public key.
     * We reconstruct the Java ECPublicKey object from those coordinates.
     */
    @SuppressWarnings("unchecked")
    private PublicKey fetchEccPublicKeyFromSupabaseJwks() throws Exception {
        String jwksEndpointUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";

        // Fetch the raw JWKS JSON from Supabase (e.g. {"keys":[{"kty":"EC","crv":"P-256","x":"...","y":"..."}]})
        String jwksJson = new String(URI.create(jwksEndpointUrl).toURL().openStream().readAllBytes());

        Map<String, Object> jwks = jsonParser.readValue(jwksJson, Map.class);
        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");

        if (keys == null || keys.isEmpty()) {
            throw new IllegalStateException("Supabase JWKS endpoint returned no signing keys");
        }

        // Supabase publishes the currently active signing key first
        Map<String, Object> currentSigningKey = keys.get(0);

        // The x and y values are the coordinates of the public point on the P-256 elliptic curve,
        // encoded as base64url strings
        String xCoordinate = (String) currentSigningKey.get("x");
        String yCoordinate = (String) currentSigningKey.get("y");

        byte[] xBytes = Base64.getUrlDecoder().decode(xCoordinate);
        byte[] yBytes = Base64.getUrlDecoder().decode(yCoordinate);

        // Reconstruct the elliptic curve point from the raw coordinates
        ECPoint publicKeyPoint = new ECPoint(new BigInteger(1, xBytes), new BigInteger(1, yBytes));

        // Load the standard P-256 curve parameters (also known as secp256r1)
        AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance("EC");
        algorithmParameters.init(new ECGenParameterSpec("secp256r1"));
        ECParameterSpec p256CurveSpec = algorithmParameters.getParameterSpec(ECParameterSpec.class);

        // Combine the point and curve spec into a full ECPublicKey
        ECPublicKeySpec ecPublicKeySpec = new ECPublicKeySpec(publicKeyPoint, p256CurveSpec);
        return KeyFactory.getInstance("EC").generatePublic(ecPublicKeySpec);
    }
}
