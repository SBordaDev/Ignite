package org.bormun.infraestructura.configs;

import org.bormun.infraestructura.seguridad.SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

                // 3. ¡LAS REGLAS DEL JUEGO!
                .authorizeHttpRequests(auth -> auth

                        // --- RUTAS PÚBLICAS (Sin token) ---
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll() // Para poder iniciar sesión
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro").permitAll() // Para crear usuarios
                        .requestMatchers(HttpMethod.GET, "/api/eventos/abiertos").permitAll() // Ver torneos disponibles

                        // --- RUTAS EXCLUSIVAS DEL ADMIN ---
                        .requestMatchers(HttpMethod.POST, "/api/eventos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/eventos/*/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/solicitudes/*/procesar").hasRole("ADMIN")

                        // --- RUTAS EXCLUSIVAS DEL EQUIPO ---
                        .requestMatchers(HttpMethod.POST, "/api/eventos/*/solicitudes").hasRole("EQUIPO")

                        // --- RUTAS COMPARTIDAS ---
                        // Ver detalle del evento (ambos pueden)
                        .requestMatchers(HttpMethod.GET, "/api/eventos/*").hasAnyRole("ADMIN", "EQUIPO")

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