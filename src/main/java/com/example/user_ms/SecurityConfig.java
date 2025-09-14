package com.example.user_ms;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity 
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserService userService) throws Exception {
        http
            .cors(c -> {})
            .authorizeHttpRequests(reg -> reg
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/me/**").authenticated()
                //.requestMatchers("/users/**").hasRole("admin") 
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));

       
        http.addFilterAfter(new PrincipalEnrichmentFilter(userService), BearerTokenAuthenticationFilter.class);

        return http.build();
    }
}