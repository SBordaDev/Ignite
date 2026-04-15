package org.bormun.infraestructura.configs;

import org.bormun.infraestructura.seguridad.SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityFilter securityFilter) throws Exception {
        return http
                // 1. Desactivamos CSRF (Cross-Site Request Forgery) porque usaremos Tokens (JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Le decimos a Spring que nuestra API es "Stateless" (Sin estado/No guarda sesiones en memoria)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))

                // 3. ¡LAS REGLAS DEL JUEGO!
                .authorizeHttpRequests(auth -> auth

                        // --- RUTAS PÚBLICAS (Sin token) ---
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/prometheus").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()

                        // --- RUTAS EXCLUSIVAS DEL ADMIN ---
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/usuarios/*/rol").hasRole("ADMIN")  // Escalar privilegios a usuarios

                        // --- RUTAS EVENTO CONTROLLER ---
                        .requestMatchers(HttpMethod.POST, "/api/eventos").hasRole("CREADOR")        // Creacion de eventos
                        .requestMatchers(HttpMethod.GET, "/api/eventos/*").permitAll()              // Info publica evento especifico
                        .requestMatchers(HttpMethod.GET, "/api/eventos/*/admin").hasRole("CREADOR") // Info privada evento creador
                        .requestMatchers(HttpMethod.GET, "/api/eventos/abiertos").permitAll()       // Ver torneos disponibles

                        // --- RUTAS SOLICITUD CONTROLLER ---
                        .requestMatchers(HttpMethod.PATCH, "/api/solicitudes/*/procesar").hasAnyRole("CREADOR", "ADMIN")// Procesar la solicitud
                        .requestMatchers(HttpMethod.POST, "/api/eventos/*/solicitudes").hasAnyRole("EQUIPO", "CREADOR") // Enviar solicitud

                        // --- RUTAS AUTH CONTROLLER ---
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()    // Para poder iniciar sesión
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro").permitAll() // Para crear usuarios
                        .requestMatchers(HttpMethod.GET, "/api/auth/usuarios/*/obtener").hasRole("ADMIN")

                        // --- CUALQUIER OTRA RUTA ---
                        // Por seguridad, si se nos olvida poner una ruta aquí, bloqueamos por defecto
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}