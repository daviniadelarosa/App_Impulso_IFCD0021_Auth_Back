package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // desactiva CSRF para APIs REST
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/register",
                                "/api/users/login",
                                "/api/users/{email}/password",
                                "/api/users/reset").permitAll() // abiertos
                        .anyRequest().authenticated() // el resto requiere auth
                )
                .httpBasic(httpBasic -> httpBasic.disable()) // desactiva login básico
                .formLogin(form -> form.disable()); // desactiva login por formulario

        return http.build();
    }
}
