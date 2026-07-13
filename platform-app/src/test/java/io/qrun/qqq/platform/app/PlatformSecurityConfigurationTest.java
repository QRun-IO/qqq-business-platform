package io.qrun.qqq.platform.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

class PlatformSecurityConfigurationTest {
  @Test
  void mapsKeycloakRealmRolesCaseInsensitively() {
    Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
        Map.of("alg", "none"), Map.of("sub", "member-1", "realm_access", Map.of("roles", List.of("member", "ADMIN"))));

    var authentication = PlatformSecurityConfiguration.jwtAuthenticationConverter().convert(jwt);

    assertThat(authentication.getAuthorities()).extracting("authority")
        .containsExactlyInAnyOrder("ROLE_MEMBER", "ROLE_ADMIN");
  }
}
