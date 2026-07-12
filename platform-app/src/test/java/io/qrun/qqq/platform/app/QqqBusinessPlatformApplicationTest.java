package io.qrun.qqq.platform.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QqqBusinessPlatformApplicationTest {

    @Autowired
    private QInstanceConfiguration.QInstance qInstance;

    @Test
    void startsWithMaterialDashboardQInstance() {
        assertThat(qInstance.name()).isEqualTo("QQQ Business Platform");
        assertThat(qInstance.frontendTheme()).isEqualTo("material-dashboard");
    }
}
