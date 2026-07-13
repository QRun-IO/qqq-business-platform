package io.qrun.qqq.platform.app;

import io.minio.MinioClient;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class PlatformInfrastructureConfiguration {
  @Bean
  MinioClient minioClient(@Value("${minio.endpoint:http://localhost:19000}") String endpoint,
      @Value("${minio.access-key:minioadmin}") String accessKey,
      @Value("${minio.secret-key:minioadmin}") String secretKey) {
    return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
  }

  @Bean
  OpenAPI platformOpenApi() {
    return new OpenAPI().info(new Info().title("QQQ Business Platform API").version("v1")
        .description("Public, member, and staff APIs for the QQQ Business Platform"))
        .components(new Components().addSecuritySchemes("bearer-jwt", new SecurityScheme()
            .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
  }
}
