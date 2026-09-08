package com.arun.aisupportplatform.config;

import com.arun.aisupportplatform.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Allow browser CORS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        // Public authentication endpoints
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login"
                        )
                        .permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Agent endpoints
                        .requestMatchers("/api/agent/**")
                        .hasAnyRole("AGENT", "ADMIN")

                        // Everything else requires authentication
                        .anyRequest()
                        .authenticated()
                )

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint((request,
                                                   response,
                                                   authException) -> {

                            response.setStatus(
                                    HttpStatus.UNAUTHORIZED.value()
                            );

                            response.setContentType(
                                    MediaType.APPLICATION_JSON_VALUE
                            );

                            response.getWriter().write(
                                    "{\"status\":401,\"message\":\"Authentication required\"}"
                            );
                        })

                        .accessDeniedHandler((request,
                                              response,
                                              accessDeniedException) -> {

                            response.setStatus(
                                    HttpStatus.FORBIDDEN.value()
                            );

                            response.setContentType(
                                    MediaType.APPLICATION_JSON_VALUE
                            );

                            response.getWriter().write(
                                    "{\"status\":403,\"message\":\"Access denied\"}"
                            );
                        })
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
        );

        configuration.setAllowedHeaders(
                List.of("*")
        );

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
                "/**",
                configuration
        );

        return source;
    }
}