package io.qrun.qqq.platform.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for QQQ Business Platform APIs
 * Generates documentation for all three API surfaces
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI businessPlatformOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("QQQ Business Platform API")
                .description("""
                    Triple API surface architecture providing:
                    
                    **Public API** (`/api/public/v1`) - Anonymous access for content and newsletter
                    - Newsletter subscription with double opt-in
                    - Published content access (pages, posts, navigation)
                    - Rate limited, CORS enabled for marketing domains
                    
                    **Member API** (`/api/member/v1`) - Authenticated member self-service
                    - Member profile management (own data only)
                    - Newsletter subscription preferences
                    - Security locks ensure data isolation (User A ≠ User B)
                    
                    **Staff API** (`/api/staff/v1`) - Administrative interface
                    - Content management (CMS, blog publishing)
                    - Newsletter campaign management
                    - CRM access (contacts/companies/deals/activities)
                    - Role-based permissions with fine-grained security locks
                    """)
                .version("1.0.0")
                .license(new License()
                    .name("Apache-2.0") 
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .servers(List.of(
                new Server().url("https://api.qrun.io").description("Production API"),
                new Server().url("https://api-staging.qrun.io").description("Staging API"),  
                new Server().url("http://localhost:8080").description("Development API")
            ))
            .addSecurityItem(new SecurityRequirement()
                .addList("memberAuth")
                .addList("staffAuth"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("memberAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("OIDC JWT token for authenticated members"))
                .addSecuritySchemes("staffAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP) 
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("OIDC JWT token for authenticated staff with RBAC roles")));
    }
}