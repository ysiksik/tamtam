package com.tamtam.api.global.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {


    @Bean
    public OpenAPI api() {
        String accessToken = "accessToken"; // 엑세스토큰 사용
        String unnecessary = "unnecessary";   // 불필요한 경우
        return new OpenAPI()
                .addSecurityItem(
                        new SecurityRequirement().addList(accessToken)
                )
                .addSecurityItem(
                        new SecurityRequirement().addList(unnecessary)
                )
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        accessToken,
                                        new SecurityScheme()
                                                .name(accessToken)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("Authorization")
                                )

                )
                ;


    }

}

