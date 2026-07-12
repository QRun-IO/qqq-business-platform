package io.qrun.qqq.platform.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"io.qrun.qqq.platform.app", "io.qrun.platform.identity", "com.qrunio.platform.newsletter"})
@EnableScheduling
public class QqqBusinessPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(QqqBusinessPlatformApplication.class, args);
    }

}
