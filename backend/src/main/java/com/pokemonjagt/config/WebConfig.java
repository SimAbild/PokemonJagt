package com.pokemonjagt.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Wires together two cross-cutting concerns:
 *
 * 1. JWT protection — the JwtInterceptor runs before every /api/** request,
 *    except /api/auth/** which must be open (you need to log in to get a token).
 *
 * 2. CORS — allows the frontend HTML files (running in the browser)
 *    to call this backend without being blocked by the browser's same-origin policy.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")           // Protect all API endpoints
                .excludePathPatterns("/api/auth/**"); // Except login/register — no token yet at that point
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")                         // Accept requests from any origin (frontend file:// or localhost)
                .allowedMethods("GET", "POST", "DELETE")
                .allowedHeaders("*");
    }
}
