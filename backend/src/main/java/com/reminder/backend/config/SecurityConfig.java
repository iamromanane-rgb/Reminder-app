package com.reminder.backend.config;

import com.reminder.backend.auth.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:3001"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST,
                                "/api/users",
                                "/api/users/login"
                        ).permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/upcoming/**").hasAnyAuthority("ACCESS_READ", "ACCESS_READ_WRITE")
                        .requestMatchers(HttpMethod.GET,"/api/users").hasAnyAuthority("ACCESS_READ","ACCESS_READ_WRITE")
                        .requestMatchers(HttpMethod.GET, "/api/users/*/events/**").hasAnyAuthority("ACCESS_READ", "ACCESS_READ_WRITE")
                        .requestMatchers(HttpMethod.POST, "/api/users/*/events/**").hasAuthority("ACCESS_READ_WRITE")
                        .requestMatchers(HttpMethod.PUT, "/api/users/*/events/**").hasAuthority("ACCESS_READ_WRITE")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/events/**").hasAuthority("ACCESS_READ_WRITE")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/*/events/**").hasAuthority("ACCESS_READ_WRITE")
                        .requestMatchers("/api/admin/**").authenticated()
                        .requestMatchers("/api/scheduler/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .anyRequest().denyAll()
                );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
