package com.lankatrust.smartbank.config;

import com.lankatrust.smartbank.security.CustomAccessDeniedHandler;
import com.lankatrust.smartbank.security.CustomAuthenticationFailureHandler;
import com.lankatrust.smartbank.security.CustomUserDetailsService;
import com.lankatrust.smartbank.security.OAuth2LoginSuccessHandler;
import com.lankatrust.smartbank.security.OtpAuthenticationSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationFailureHandler authenticationFailureHandler;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OtpAuthenticationSuccessHandler otpAuthenticationSuccessHandler;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/home", "/login", "/login/otp", "/login/otp/verify", "/login/otp/resend",
                                "/register", "/forgot-password", "/reset-password", "/access-denied",
                                "/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**", "/error",
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                                "/api/auth/**", "/api/health").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/WEB-INF/**").permitAll()
                        .requestMatchers("/customer/profile/**", "/customer/change-password/**").authenticated()

                        .requestMatchers("/customer/**").hasAnyRole("CUSTOMER", "BANK_MANAGER")
                        .requestMatchers("/officer/**").hasAnyRole("BANK_OFFICER", "BANK_MANAGER")
                        .requestMatchers("/cre/**", "/customer-relations/**").hasAnyRole("CUSTOMER_RELATIONS_EXECUTIVE", "BANK_MANAGER")
                        .requestMatchers("/admin/**", "/audit-logs/**").hasAnyRole("SYSTEM_ADMINISTRATOR", "BANK_MANAGER")
                        .requestMatchers("/compliance/**").hasAnyRole("COMPLIANCE_OFFICER", "BANK_MANAGER")
                        .requestMatchers("/manager/**", "/approvals/**", "/branches/**").hasRole("BANK_MANAGER")
                        .requestMatchers("/staff/**", "/accounts/manage/**", "/reports/**", "/accounts/open/**")
                        .hasAnyRole("BANK_OFFICER", "BANK_MANAGER", "SYSTEM_ADMINISTRATOR")

                        .requestMatchers("/dashboard/**", "/bankdash/**", "/profile/**", "/notifications/**", "/logout").authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(otpAuthenticationSuccessHandler)
                        .failureHandler(authenticationFailureHandler)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .invalidSessionUrl("/login?expired=true")
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry())
                        .expiredUrl("/login?expired=true")
                )
                .authenticationProvider(authenticationProvider())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }

    @Bean
    public org.springframework.security.access.hierarchicalroles.RoleHierarchy roleHierarchy() {
        org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl hierarchy =
                new org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl();
        hierarchy.setHierarchy(
                "ROLE_BANK_MANAGER > ROLE_SYSTEM_ADMINISTRATOR\n" +
                        "ROLE_BANK_MANAGER > ROLE_BANK_OFFICER\n" +
                        "ROLE_BANK_MANAGER > ROLE_COMPLIANCE_OFFICER\n" +
                        "ROLE_BANK_MANAGER > ROLE_CUSTOMER_RELATIONS_EXECUTIVE\n" +
                        "ROLE_BANK_MANAGER > ROLE_CUSTOMER\n" +
                        "ROLE_SYSTEM_ADMINISTRATOR > ROLE_CUSTOMER"
        );
        return hierarchy;
    }
}
