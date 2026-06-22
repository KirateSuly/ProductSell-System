package com.productsell.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger / SpringDoc OpenAPI 配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("商品销售管理系统 API")
                        .description("商品销售管理系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("Developer")))
                // JWT 认证配置
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .name("Bearer")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
