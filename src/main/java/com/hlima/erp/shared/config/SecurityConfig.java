package com.hlima.erp.shared.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlima.erp.auth.security.JwtAuthenticationFilter;
import com.hlima.erp.shared.exception.ApiError;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Login não passa pelo AuthenticationManager do Spring (AuthService valida
// a senha direto com o PasswordEncoder, pra poder controlar o bloqueio por
// tentativas erradas) — por isso não há bean de AuthenticationProvider
// aqui, só o necessário pra validar o JWT nas rotas protegidas.
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/auth/refresh",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    // Origem(ns) do frontend (Vite dev server por padrão) liberada(s) pra
    // chamar a API via fetch/axios do navegador — separado por vírgula.
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // Sem isso, o Spring Security cai no AuthenticationEntryPoint default
    // (Http403ForbiddenEntryPoint) pra qualquer requisição sem autenticação
    // válida — inclusive access token expirado — devolvendo 403 puro (sem
    // passar pelo GlobalExceptionHandler). O frontend só tenta renovar a
    // sessão via /auth/refresh em cima de um 401 (ver api.ts); um 403 nesse
    // caso não é "sem permissão", é "não autenticado", e sem tratamento aqui
    // vira um "Ocorreu um erro inesperado." confuso pro usuário no meio do
    // uso, em vez de renovar o token ou mandar pro login de novo.
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            ApiError body = ApiError.of(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Não autenticado",
                    "Sessão expirada ou token inválido. Faça login novamente.",
                    request.getRequestURI()
            );
            objectMapper.writeValue(response.getWriter(), body);
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtAuthenticationFilter jwtFilter, AuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
