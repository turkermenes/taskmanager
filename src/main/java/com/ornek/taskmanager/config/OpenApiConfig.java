package com.ornek.taskmanager.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI taskManagerOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Task Manager API")
                        .version("v1")
                        .description("Görev yönetimi için REST API")
                        .contact(new Contact()
                                .name("Enes Türker")
                                .url("https://github.com/turkermenes")));
    }
}
