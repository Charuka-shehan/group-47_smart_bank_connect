package com.lankatrust.smartbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * SmartBank Connect - Web-Based Banking Management System
 * LankaTrust Bank PLC | Group 47 | SE2030 Software Engineering
 *
 * Entry point for the Spring Boot application. Extends
 * SpringBootServletInitializer so the app can also be deployed
 * as a traditional WAR to an external servlet container if needed.
 */
@SpringBootApplication
public class SmartBankConnectApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(SmartBankConnectApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SmartBankConnectApplication.class, args);
    }
}
