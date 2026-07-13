package io.qrun.qqq.platform.app;

import io.qrun.platform.identity.auth.CrmContactSynchronizer;
import io.qrun.platform.identity.auth.JdbcUserProfileRepository;
import io.qrun.platform.identity.auth.OidcClaims;
import io.qrun.platform.identity.auth.PlatformAuthCustomizer;
import io.qrun.platform.identity.auth.RolePermissionBundleResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.http.HttpMethod;

@Configuration
@EnableMethodSecurity
class PlatformSecurityConfiguration {
  @Bean
  PlatformAuthCustomizer platformAuthCustomizer(DataSource dataSource) {
    return new PlatformAuthCustomizer(new JdbcUserProfileRepository(dataSource),
        new RolePermissionBundleResolver(Map.of(
            "MEMBER", List.of("profile.read", "profile.edit", "newsletter.read", "content.write"),
            "STAFF", List.of("profile.read", "content.manage", "newsletter.manage", "users.manage"),
            "ADMIN", List.of("platform.admin"))), CrmContactSynchronizer.NO_OP);
  }

  @Bean
  PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(12); }

  @Bean
  JwtDecoder jwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
      @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}") String jwkSetUri,
      @Value("${security.jwt.audience:qqq-platform-api}") String audience) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    OAuth2TokenValidator<Jwt> audienceValidator = token -> token.getAudience().contains(audience)
        ? OAuth2TokenValidatorResult.success()
        : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Required audience is missing", null));
    decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
        JwtValidators.createDefaultWithIssuer(issuer), audienceValidator));
    return decoder;
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowCredentials(false); // Set to false for public API
    configuration.addAllowedOrigin("http://localhost:3004"); // Frontend URL
    configuration.addAllowedOrigin("http://localhost:3005"); // Backup frontend URL
    configuration.addAllowedOriginPattern("http://localhost:*"); // Allow any localhost port
    configuration.addAllowedHeader("*");
    configuration.addAllowedMethod("*");
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/public/**", configuration);
    return source;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, PlatformAuthCustomizer customizer) throws Exception {
    return http.csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll() // Allow CORS preflight
            .requestMatchers("/api/public/v1/**", "/health", "/actuator/health/**", "/actuator/info", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            .requestMatchers("/api/member/v1/**").hasAnyRole("MEMBER", "STAFF", "ADMIN")
            .requestMatchers("/api/staff/v1/**").hasAnyRole("STAFF", "ADMIN")
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .addFilterAfter(new IdentitySessionFilter(customizer), org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
        .build();
  }

  @Bean
  static JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRolesConverter());
    return converter;
  }

  private static final class KeycloakRealmRolesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override public Collection<GrantedAuthority> convert(Jwt jwt) {
      List<GrantedAuthority> result = new ArrayList<>();
      Object realm = jwt.getClaim("realm_access");
      if (realm instanceof Map<?, ?> values && values.get("roles") instanceof Collection<?> roles) {
        roles.stream().filter(String.class::isInstance).map(String.class::cast)
            .map(role -> "ROLE_" + role.toUpperCase(Locale.ROOT)).distinct()
            .map(SimpleGrantedAuthority::new).forEach(result::add);
      }
      return result;
    }
  }

  private static final class IdentitySessionFilter extends OncePerRequestFilter {
    private final PlatformAuthCustomizer customizer;
    private IdentitySessionFilter(PlatformAuthCustomizer customizer) { this.customizer = customizer; }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
      var authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
        var qSession = customizer.customize(new com.kingsrook.qqq.backend.core.model.session.QSession(), OidcClaims.from(jwt.getClaims()));
        request.setAttribute("qqqSession", qSession);
        request.setAttribute("userProfileId", qSession.getValue(PlatformAuthCustomizer.USER_PROFILE_ID_SESSION_VALUE));
      }
      chain.doFilter(request, response);
    }
  }
}
