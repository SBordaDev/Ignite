package org.bormun.infraestructura.seguridad;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.dominio.repositorios.UsuarioRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtenemos el token de la cabecera de la petición
        String tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {
            // 2. Usamos nuestra máquina para validar el token y sacar el correo
            String emailUsuario = tokenService.getSubject(tokenJWT);

            if (emailUsuario != null) {
                // 3. Buscamos al usuario en la base de datos
                UsuarioEntidad usuario = usuarioRepository.findByEmail(emailUsuario).orElseThrow();

                // 4. Traducimos nuestro Rol al idioma que Spring Security entiende (Agregando "ROLE_")
                var authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

                // 5. Creamos el "Pase de Entrada" oficial de Spring
                var authentication = new UsernamePasswordAuthenticationToken(usuario, null, authorities);

                // 6. ¡Le decimos al Guardia que lo deje pasar!
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 7. Continúa con el flujo normal de la petición (hacia el siguiente filtro o el controlador)
        filterChain.doFilter(request, response);
    }

    // Método auxiliar para limpiar el texto de la cabecera
    private String recuperarToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        // El estándar mundial dicta que el token se envía así: "Bearer eyJhbG..."
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.replace("Bearer ", ""); // Le quitamos la palabra y dejamos solo el código
        }
        return null;
    }
}