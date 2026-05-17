package com.pokemonjagt.config;

import com.pokemonjagt.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, BasicAuthenticationFilter.class)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SignatureAlgorithm.ES256)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}

/*
 * FORKLARING AF SecurityConfig
 *
 * SecurityFilterChain    - definerer reglerne for alle indkommende requests
 *                          samme mønster som ProjectSecurityConfig i undervisningen
 *
 * csrf.disable()         - vi bruger JWT i header, ikke cookies — CSRF irrelevant
 *
 * STATELESS              - serveren gemmer ingen session — brugeren sender JWT ved hvert kald
 *
 * permitAll()            - /api/auth/** er åbent — man har ingen token endnu ved login
 * authenticated()        - alle andre endpoints kræver en gyldig JWT
 *
 * addFilterBefore()      - tilføjer vores JwtAuthenticationFilter til filterkæden
 *                          kører FØR BasicAuthenticationFilter — samme princip som i undervisningen
 *
 * jwtDecoder()           - bygger den decoder der validerer Supabase-tokens
 *                          Supabase bruger ES256 (EC-algoritmen) — Spring understøtter kun RS256 som default
 *                          vi specificerer ES256 eksplicit så Nimbus kan finde den rigtige nøgle i JWKS
 *
 * corsConfigurationSource - tillader browseren at kalde vores API på tværs af domæner/porte
 *                           konfigureres i Security fordi filtrene kører FØR MVC-laget
 */
