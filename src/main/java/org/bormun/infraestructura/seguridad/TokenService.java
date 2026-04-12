package org.bormun.infraestructura.seguridad;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    // Traemos el secreto desde el application.properties
    @Value("${api.security.token.secret}")
    private String apiSecret;

    // 1. EL GENERADOR DE MANILLAS
    public String generarToken(UsuarioEntidad usuario) {
        try {
            // El algoritmo de encriptación usando nuestro secreto
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);

            return JWT.create()
                    .withIssuer("ignite_api") // Quién emite el token
                    .withSubject(usuario.getEmail()) // A quién le pertenece (El correo)
                    .withClaim("rol", usuario.getRol().name()) // ¡Guardamos el ROL en el token!
                    .withExpiresAt(generarFechaExpiracion()) // ¿Cuándo caduca?
                    .sign(algorithm); // Lo sellamos

        } catch (JWTCreationException exception){
            throw new RuntimeException("Error al generar el token JWT", exception);
        }
    }

    // 2. EL VERIFICADOR DE MANILLAS (Devuelve el correo si es válido)
    public String getSubject(String token) {
        if (token == null) {
            throw new RuntimeException("El token es nulo");
        }

        try {
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            DecodedJWT verifier = JWT.require(algorithm)
                    .withIssuer("ignite_api")
                    .build()
                    .verify(token); // Si el token fue alterado, esto explota y va al catch

            return verifier.getSubject(); // Retorna el correo del usuario

        } catch (JWTVerificationException exception) {
            return null; // Si es inválido o expiró, devolvemos null y le negamos la entrada
        }
    }

    // 3. REGLA DE NEGOCIO: Los tokens duran 1 horas
    private Instant generarFechaExpiracion() {
        // Usamos el huso horario de Colombia (-05:00)
        return LocalDateTime.now().plusHours(1).toInstant(ZoneOffset.of("-05:00"));
    }
}