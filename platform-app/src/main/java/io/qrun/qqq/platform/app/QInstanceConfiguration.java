package io.qrun.qqq.platform.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QInstanceConfiguration {

    @Bean
    QInstance qInstance() {
        return new QInstance("QQQ Business Platform", "material-dashboard");
    }

    public record QInstance(String name, String frontendTheme) { }
}
