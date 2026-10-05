package com.lankatrust.smartbank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SmartBank Connect REST API",
                version = "1.0.0",
                description = "Production-quality banking APIs for LankaTrust Bank PLC",
                contact = @Contact(
                        name = "Group 47 - SE2030 Support Team",
                        email = "support@lankatrust.lk"
                )
        )
)
public class OpenApiConfig {
}
