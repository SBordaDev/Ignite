package org.bormun.evento;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.bormun.aplicacion.usecase.EnviarSolicitud;
import org.bormun.aplicacion.usecase.ProcesarSolicitud;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.presentacion.controladores.SolicitudController;
import org.bormun.presentacion.dto.request.ProcesarSolicitudDTO;
import org.bormun.presentacion.dto.request.SolicitudRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SolicitudControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private SolicitudController solicitudController;

    @Mock
    private EnviarSolicitud enviarSolicitudUseCase;

    @Mock
    private ProcesarSolicitud procesarSolicitudUseCase;

    @Mock
    private RateLimiterRegistry rateLimiterRegistry;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(solicitudController)
                .build();
    }

    @Test
    void enviar_DeberiaRetornar201_CuandoSolicitudEsValida() throws Exception {
        RateLimiter mockRateLimiter = mock(RateLimiter.class);
        when(mockRateLimiter.acquirePermission()).thenReturn(true);
        when(rateLimiterRegistry.rateLimiter(anyString(), anyString())).thenReturn(mockRateLimiter);

        String jsonRequest = """
                {
                  "nombreOrganizacion": "Equipo Alpha",
                  "equipo": {
                    "nombreEquipo": "Los Titanes",
                    "integrantes": [
                      {
                        "nombre": "Samuel",
                        "identificacion": 123456,
                        "generoNacimiento": "HOMBRE",
                        "fechaNacimiento": "2004-05-10"
                      }
                    ]
                  },
                  "idCategoria": 1
                }
                """;

        doNothing().when(enviarSolicitudUseCase)
                .ejecutar(eq(1L), any(SolicitudRequestDTO.class));

        mockMvc.perform(post("/api/eventos/1/solicitudes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje")
                        .value("Solicitud enviada correctamente y en proceso de revisión."));
    }

    @Test
    void procesar_DeberiaRetornar200_CuandoSolicitudEsAceptada() throws Exception {

        String jsonRequest = """
                {
                  "estado": "ACEPTADO"
                }
                """;

        UsuarioEntidad usuario = new UsuarioEntidad();

        doNothing().when(procesarSolicitudUseCase).ejecutar(
                eq(1L),
                any(ProcesarSolicitudDTO.class),
                any(UsuarioEntidad.class));

        mockMvc.perform(patch("/api/solicitudes/1/procesar")
                        .principal(() -> "usuario")
                        .requestAttr(
                                "usuarioAutenticado",
                                usuario
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje")
                        .value("Solicitud aceptada exitosamente"));
    }

    @Test
    void procesar_DeberiaRetornar200_CuandoSolicitudEsRechazada() throws Exception {

        String jsonRequest = """
                {
                  "estado": "RECHAZADO"
                }
                """;

        UsuarioEntidad usuario = new UsuarioEntidad();

        doNothing().when(procesarSolicitudUseCase).ejecutar(
                eq(1L),
                any(ProcesarSolicitudDTO.class),
                any(UsuarioEntidad.class));

        mockMvc.perform(patch("/api/solicitudes/1/procesar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensaje")
                        .value("Solicitud rechazada exitosamente."));
    }
}