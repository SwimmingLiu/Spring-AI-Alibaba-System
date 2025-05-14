package com.swimmingliu.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    // 设置 openapi 基础参数
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Spring-AI-Alibaba-Deepseek 接口管理")
                        .contact(new Contact().name("Spring-AI-Alibaba-Deepseek")
                                .email("swimmingliu@outlook.com")
                                .url("https://swimmingliu.cn"))
                        .version("1.0")
                        .description( "Spring-AI-Alibaba-Deepseek 接口管理")
                        .license(new License().name("Apache 2.0")));
    }
}
