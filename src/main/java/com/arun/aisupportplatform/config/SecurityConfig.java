package com.arun.aisupportplatform.config;


import com.arun.aisupportplatform.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .cors(cors -> cors.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login"
                        ).permitAll()

                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/agent/**")
                        .hasAnyRole("AGENT", "ADMIN")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint((request,
                                                   response,
                                                   authException) -> {

                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
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

                            response.setStatus(HttpStatus.FORBIDDEN.value());
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
}
