package io.github.chiang_sh.file_nest.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String JWT_SECURITY_SCHEMA = "bearerAuth";

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        JWT_SECURITY_SCHEMA,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(JWT_SECURITY_SCHEMA))
                .info(new Info().title("File Nest API").version("1.0"));
    }
}
