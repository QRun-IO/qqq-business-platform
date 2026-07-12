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
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableMethodSecurity
class PlatformSecurityConfiguration {

  @Bean
  PlatformAuthCustomizer platformAuthCustomizer(DataSource dataSource) {
    return new PlatformAuthCustomizer(
        new JdbcUserProfileRepository(dataSource),
        new RolePermissionBundleResolver(Map.of(
            "MEMBER", List.of("profile.read", "profile.edit", "newsletter.read"),
            "STAFF", List.of("profile.read", "content.manage"),
            "ADMIN", List.of("platform.admin"))),
        CrmContactSynchronizer.NO_OP);
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, PlatformAuthCustomizer customizer) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/public/v1/**", "/health", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .requestMatchers("/api/member/v1/**").hasRole("MEMBER")
            .requestMatchers("/api/staff/v1/**").hasRole("STAFF")
            .anyRequest().authenticated())
        .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
        .addFilterAfter(new IdentitySessionFilter(customizer), org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)
        .build();
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
    authorities.setAuthorityPrefix("ROLE_");
    authorities.setAuthoritiesClaimName("realm_access.roles");
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authorities);
    return converter;
  }

  private static final class IdentitySessionFilter extends OncePerRequestFilter {
    private final PlatformAuthCustomizer customizer;

    private IdentitySessionFilter(PlatformAuthCustomizer customizer) {
      this.customizer = customizer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
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
