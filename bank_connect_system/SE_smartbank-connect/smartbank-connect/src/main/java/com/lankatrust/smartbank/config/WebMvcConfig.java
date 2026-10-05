package com.lankatrust.smartbank.config;

import com.lankatrust.smartbank.security.OtpInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final OtpInterceptor otpInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(otpInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/home", "/login", "/login/otp", "/login/otp/verify", "/login/otp/resend",
                        "/register", "/forgot-password", "/reset-password", "/access-denied",
                        "/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**", "/error",
                        "/api/**", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/logout");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
    }
}
