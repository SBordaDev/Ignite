package org.bormun.evento;

import org.bormun.dominio.modelos.Roles;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.infraestructura.seguridad.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @InjectMocks
    private TokenService tokenService;

    private UsuarioEntidad usuarioMock;

    @BeforeEach
    void setUp() {
        // Inyectamos a la fuerza el valor de @Value("${api.security.secret}")
        ReflectionTestUtils.setField(tokenService, "apiSecret", "1234567890-mi-clave-secreta-de-prueba-muy-larga-para-hs256");

        usuarioMock = new UsuarioEntidad();
        ReflectionTestUtils.setField(usuarioMock, "id", 1L);
        usuarioMock.setEmail("admin_test@gmail.com");
        usuarioMock.setRol(Roles.ADMIN);
    }

    @Test
    void generarToken_DebeRetornarUnStringNoNulo() {
        String token = tokenService.generarToken(usuarioMock);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void getSubject_DebeRetornarNombreUsuario_CuandoTokenEsValido() {
        String token = tokenService.generarToken(usuarioMock);

        String subject = tokenService.getSubject(token);

        assertEquals("admin_test@gmail.com", subject);
    }

    @Test
    void getSubject_DebeLanzarExcepcion_CuandoTokenEsInvalido() {
        String tokenInvalido = "este.token.es.totalmente.invalido";

        assertThrows(RuntimeException.class, () -> {
            tokenService.getSubject(tokenInvalido);
        });
    }
}
