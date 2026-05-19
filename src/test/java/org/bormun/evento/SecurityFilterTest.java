package org.bormun.evento;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.bormun.aplicacion.repositorios.UsuarioRepository;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.infraestructura.seguridad.SecurityFilter;
import org.bormun.infraestructura.seguridad.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private SecurityFilter securityFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void doFilterInternal_DebeContinuarFiltro_YNoAutenticar_CuandoNoHayHeaderAuthorization() throws Exception {
        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DebeAutenticarEnElContexto_CuandoTokenEsValido() throws Exception {
        String tokenBearer = "Bearer token_simulado_valido";
        String tokenLimpio = "token_simulado_valido";
        String email = "admin_test@gmail.com";

        request.addHeader("Authorization", tokenBearer);

        UsuarioEntidad usuarioBd = new UsuarioEntidad();
        usuarioBd.setEmail(email);
        usuarioBd.setRol(org.bormun.dominio.modelos.Roles.ADMIN);

        when(tokenService.getSubject(tokenLimpio)).thenReturn(email);
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioBd));

        securityFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_DebeRetornar401_YDetenerPeticion_CuandoTokenEsInvalido() throws Exception {
        // Arrange
        String tokenBearer = "Bearer token_simulado_invalido";
        String tokenLimpio = "token_simulado_invalido";
        String mensajeErrorReal = "The Token has expired";

        request.addHeader("Authorization", tokenBearer);

        // Simulamos el comportamiento real del servicio lanzando la excepción
        when(tokenService.getSubject(tokenLimpio))
                .thenThrow(new JWTVerificationException(mensajeErrorReal));

        // Act
        securityFilter.doFilterInternal(request, response, filterChain);

        // Assert
        // 1. Verificamos que la petición murió ahí y NO avanzó al controlador
        verify(filterChain, never()).doFilter(request, response);

        // 2. Verificamos que nadie fue autenticado
        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // 3. Verificamos que el cliente recibe un 401 Unauthorized
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json;charset=UTF-8", response.getContentType());

        // 4. Verificamos que el JSON enviado contiene los mensajes correctos
        String jsonRespuesta = response.getContentAsString();
        assertTrue(jsonRespuesta.contains("\"error\": \"Acceso Denegado\""));
        assertTrue(jsonRespuesta.contains(mensajeErrorReal));
    }
}
