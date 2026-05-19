package org.bormun.infraestructura.seguridad;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.aplicacion.repositorios.UsuarioRepository;
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
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 1. Obtenemos el token de la cabecera de la petición
        String tokenJWT = recuperarToken(request);

        if (tokenJWT != null) {

            // 2. Usamos nuestra máquina para validar el token y sacar el correo
            try{
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
            } catch (JWTVerificationException e){
                System.err.println("Intento de acceso rechazado: Token inválido o expirado. Detalles: " + e.getMessage());

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Retorna un código 401
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String jsonBody = String.format("{\"error\": \"Acceso Denegado\", \"mensaje\": \"%s\"}", e.getMessage());
                response.getWriter().write(jsonBody);

                //Detenemos la cadena aquí mismo. La petición muere y retorna al cliente.
                return;
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