package io.qrun.qqq.platform.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
    "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:9/jwks",
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://test-issuer"
})
class QqqBusinessPlatformApplicationTest {

    @Autowired
    private QInstanceConfiguration.QInstance qInstance;

    @Test
    void startsWithMaterialDashboardQInstance() {
        assertThat(qInstance.name()).isEqualTo("QQQ Business Platform");
        assertThat(qInstance.frontendTheme()).isEqualTo("material-dashboard");
    }
}
