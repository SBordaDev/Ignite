package org.bormun.evento;

import org.bormun.aplicacion.usecase.ConsultarUsuario;
import org.bormun.aplicacion.repositorios.UsuarioRepository;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.infraestructura.seguridad.TokenService;
import org.bormun.presentacion.GlobalExceptionHandler;
import org.bormun.presentacion.controladores.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuthController authController;

    @Mock private AuthenticationManager authenticationManager;
    @Mock private TokenService tokenService;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ConsultarUsuario consultarUsuario;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_DeberiaRetornar200_CuandoCredencialesSonCorrectas() throws Exception {

        String jsonLogin = """
        {
          "email":"admin@gmail.com",
          "password":"123456"
        }
        """;

        UsuarioEntidad usuario = new UsuarioEntidad();
        usuario.setEmail("admin@gmail.com");
        usuario.setPassword("hash_guardado");

        when(usuarioRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("123456", "hash_guardado"))
                .thenReturn(true);

        when(tokenService.generarToken(usuario))
                .thenReturn("token_falso");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin))
                .andExpect(status().isOk());
    }

    @Test
    void registrar_DeberiaRetornar201_CuandoRegistroEsExitoso() throws Exception {
        // Arrange: Ajusta el JSON según tu RegistroRequestDTO
        String jsonRegistro = """
                {
                  "nombre": "Samuel",
                  "email": "samuel@test.com",
                  "password": "pass",
                  "rol": "EQUIPO"
                }
                """;

        // Simular que el usuario no existe para que se pueda guardar
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("hashed_pass");

        // Act & Assert
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro))
                .andExpect(status().isCreated());
    }
}