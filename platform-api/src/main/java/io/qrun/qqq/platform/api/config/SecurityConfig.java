package io.qrun.qqq.platform.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for triple API surface architecture
 * - /api/public/v1 - Anonymous access
 * - /api/member/v1 - Authenticated members with session cookies
 * - /api/staff/v1 - Authenticated staff with RBAC permissions
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF protection - disabled for APIs, will use double-submit cookie pattern
            .csrf(csrf -> csrf.disable())
            
            // Session management - stateless for JWT/cookie based auth
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Authorization rules
            .authorizeHttpRequests(authz -> authz
                // Public API - no authentication required
                .requestMatchers("/api/public/v1/**").permitAll()
                
                // Health and actuator endpoints
                .requestMatchers("/health", "/actuator/**").permitAll()
                
                // OpenAPI documentation 
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Member API - requires MEMBER role
                .requestMatchers("/api/member/v1/**").hasRole("MEMBER")
                
                // Staff API - requires STAFF role  
                .requestMatchers("/api/staff/v1/**").hasRole("STAFF")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // OAuth2 Resource Server configuration for JWT validation
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            
            build();
    }
    
    /**
     * JWT Authentication Converter
     * Maps Keycloak roles and claims to Spring Security authorities
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        
        // Map Keycloak realm roles to Spring Security roles
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
        
        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        
        return authenticationConverter;
    }
    
    /**
     * CORS Configuration for different API surfaces
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Public API CORS - allow marketing website domain
        CorsConfiguration publicConfig = new CorsConfiguration();
        publicConfig.setAllowedOriginPatterns(List.of(
            "https://*.qrun.io",  // Production marketing domain
            "http://localhost:*"   // Development
        ));
        publicConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        publicConfig.setAllowedHeaders(List.of("*"));
        publicConfig.setAllowCredentials(false); // No credentials for public API
        
        // Member API CORS - allow member portal domain
        CorsConfiguration memberConfig = new CorsConfiguration();
        memberConfig.setAllowedOriginPatterns(List.of(
            "https://app.qrun.io", // Production member portal
            "http://localhost:*"    // Development  
        ));
        memberConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        memberConfig.setAllowedHeaders(List.of("*"));
        memberConfig.setAllowCredentials(true); // Allow session cookies
        
        // Staff API CORS - allow admin domain
        CorsConfiguration staffConfig = new CorsConfiguration();
        staffConfig.setAllowedOriginPatterns(List.of(
            "https://admin.qrun.io", // Production admin portal
            "http://localhost:*"      // Development
        ));
        staffConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        staffConfig.setAllowedHeaders(List.of("*"));
        staffConfig.setAllowCredentials(true); // Allow session cookies
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/public/v1/**", publicConfig);
        source.registerCorsConfiguration("/api/member/v1/**", memberConfig);
        source.registerCorsConfiguration("/api/staff/v1/**", staffConfig);
        
        return source;
    }
}