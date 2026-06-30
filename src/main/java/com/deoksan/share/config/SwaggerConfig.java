package com.deoksan.share.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 문서 설정.
 *
 * 모든 값은 AppProperties에서 읽어오므로, application.properties 또는
 * 환경변수로 변경 가능하다.
 *
 * 서버 실행 후 Swagger UI 접근:
 * - {@code {app.base-url}/swagger-ui.html}
 * - {@code {app.base-url}/api-docs} (raw OpenAPI 스펙)
 *
 * build.gradle 의존성:
 * <pre>
 * implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.0'
 * </pre>
 */
@Configuration
public class SwaggerConfig {

    private final AppProperties props;

    public SwaggerConfig(AppProperties props) {
        this.props = props;
    }

    @Bean
    public OpenAPI personalBlogOpenAPI() {
        String securitySchemeName = "bearerAuth";

        Contact contact = new Contact()
                .name(props.getAuthorName());

        if (props.getGithubUrl() != null && !props.getGithubUrl().isBlank()) {
            contact.url(props.getGithubUrl());
        }

        return new OpenAPI()
                .info(new Info()
                        .title(props.getSwaggerTitle())
                        .description(props.getSwaggerDescription())
                        .version(props.getSwaggerVersion())
                        .contact(contact))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
