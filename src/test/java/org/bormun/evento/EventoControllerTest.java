package org.bormun.evento;

import org.bormun.aplicacion.usecase.ConsultarEvento;
import org.bormun.aplicacion.usecase.CrearEvento;
import org.bormun.dominio.modelos.Evento;
import org.bormun.dominio.modelos.Roles;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.presentacion.controladores.EventoController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EventoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CrearEvento crearEventoUseCase;

    @Mock
    private ConsultarEvento consultarEventoUseCase;

    @InjectMocks
    private EventoController eventoController;

    // Variable dinámica para simular diferentes estados de sesión por prueba (Admin, Dueño, Otro)
    private UsuarioEntidad usuarioAutenticadoMock;

    @BeforeEach
    void setUp() {
        // Inicializamos un usuario por defecto
        usuarioAutenticadoMock = new UsuarioEntidad("test@test.com", "password", Roles.CREADOR);
        ReflectionTestUtils.setField(usuarioAutenticadoMock, "id", 1L);

        mockMvc = MockMvcBuilders.standaloneSetup(eventoController)
                // Inyectamos el @AuthenticationPrincipal de forma nativa sin levantar Spring Security
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterAnnotation(AuthenticationPrincipal.class) != null;
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return usuarioAutenticadoMock;
                    }
                })
                .build();
    }

    // =========================================================================
    // TESTS PARA: POST /api/eventos
    // =========================================================================

    @Test
    void crearNuevoEvento_DeberiaRetornar201_CuandoEsExitoso() throws Exception {
        // Arrange
        // JSON mínimo válido para que EventoMapper.aDominio(dto) no lance NullPointerException
        String jsonRequest = "{\n" +
                "  \"nombre\": \"Torneo de Verano\",\n" +
                "  \"categorias\": []\n" +
                "}";

        Evento eventoMock = new Evento("Torneo de Verano");
        eventoMock.setId(100L); // Simulamos que la BD le asignó el ID 100

        when(crearEventoUseCase.crearEvento(any(Evento.class), any(UsuarioEntidad.class)))
                .thenReturn(eventoMock);

        // Act & Assert
        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensaje").value("Evento creado exitosamente"))
                .andExpect(jsonPath("$.eventoId").value(100));
    }

    @Test
    void crearNuevoEvento_DeberiaRetornar400_CuandoFallaValidacionDeNegocio() throws Exception {
        // Arrange
        String jsonRequest = "{\n" +
                "  \"nombre\": \"Torneo de Verano\",\n" +
                "  \"categorias\": []\n" +
                "}";

        when(crearEventoUseCase.crearEvento(any(Evento.class), any(UsuarioEntidad.class)))
                .thenThrow(new IllegalArgumentException("El nombre del evento ya existe"));

        // Act & Assert
        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El nombre del evento ya existe"));
    }

    @Test
    void crearNuevoEvento_DeberiaRetornar500_CuandoHayErrorInterno() throws Exception {
        // Arrange
        String jsonRequest = "{\n" +
                "  \"nombre\": \"Torneo de Verano\",\n" +
                "  \"categorias\": []\n" +
                "}";

        when(crearEventoUseCase.crearEvento(any(Evento.class), any(UsuarioEntidad.class)))
                .thenThrow(new RuntimeException("Falla de base de datos"));

        // Act & Assert
        mockMvc.perform(post("/api/eventos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Ocurrió un error en el servidor al guardar el evento"));
    }

    // =========================================================================
    // TESTS PARA: GET /api/eventos/abiertos
    // =========================================================================

    @Test
    void listarAbiertos_DeberiaRetornarListaDeEventosY200() throws Exception {
        // Arrange
        EventoEntidad evento1 = new EventoEntidad();
        evento1.setId(1L);
        evento1.setNombre("Evento 1");
        evento1.setInscripcionAbierta(true);

        when(consultarEventoUseCase.listarAbiertos()).thenReturn(List.of(evento1));

        // Act & Assert
        mockMvc.perform(get("/api/eventos/abiertos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Evento 1"))
                .andExpect(jsonPath("$[0].inscripcionAbierta").value(true));
    }

    // =========================================================================
    // TESTS PARA: GET /api/eventos/{id} (Detalle Público)
    // =========================================================================

    @Test
    void obtenerDetallePublico_DeberiaRetornarEventoY200_CuandoExiste() throws Exception {
        // Arrange
        EventoEntidad entidad = new EventoEntidad();
        entidad.setId(5L);
        entidad.setNombre("Torneo Nacional");

        when(consultarEventoUseCase.obtenerPorId(5L)).thenReturn(entidad);

        // Act & Assert
        mockMvc.perform(get("/api/eventos/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nombre").value("Torneo Nacional"));
    }

    @Test
    void obtenerDetallePublico_DeberiaRetornar404_CuandoNoExiste() throws Exception {
        // Arrange
        when(consultarEventoUseCase.obtenerPorId(99L))
                .thenThrow(new IllegalArgumentException("El evento con el id (99) no fue encontrado"));

        // Act & Assert
        mockMvc.perform(get("/api/eventos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("El evento con el id (99) no fue encontrado"));
    }

    // =========================================================================
    // TESTS PARA: GET /api/eventos/{id}/creador
    // =========================================================================

    @Test
    void obtenerDetalleCreador_DeberiaRetornar200_CuandoElUsuarioEsAdmin() throws Exception {
        // Arrange: El usuario logueado es ADMIN
        usuarioAutenticadoMock.setRol(Roles.ADMIN);
        ReflectionTestUtils.setField(usuarioAutenticadoMock, "id", 10L);// ID del admin

        EventoEntidad entidad = new EventoEntidad();
        entidad.setId(7L);
        entidad.setNombre("Torneo Especial");

        // El dueño del evento es otro usuario (ID 20)
        UsuarioEntidad dueño = new UsuarioEntidad();
        ReflectionTestUtils.setField(dueño, "id", 20L);
        entidad.setCreador(dueño);

        when(consultarEventoUseCase.obtenerPorId(7L)).thenReturn(entidad);

        // Act & Assert
        mockMvc.perform(get("/api/eventos/7/creador"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void obtenerDetalleCreador_DeberiaRetornar200_CuandoElUsuarioEsElDueño() throws Exception {
        // Arrange: El usuario logueado es normal (USER) con ID 5
        usuarioAutenticadoMock.setRol(Roles.EQUIPO);
        ReflectionTestUtils.setField(usuarioAutenticadoMock, "id", 5L);;

        EventoEntidad entidad = new EventoEntidad();
        entidad.setId(8L);

        // El creador de la entidad tiene el mismo ID que el usuario logueado
        UsuarioEntidad dueño = new UsuarioEntidad();
        ReflectionTestUtils.setField(dueño, "id", 5L);
        entidad.setCreador(dueño);

        when(consultarEventoUseCase.obtenerPorId(8L)).thenReturn(entidad);

        // Act & Assert
        mockMvc.perform(get("/api/eventos/8/creador"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8));
    }

    @Test
    void obtenerDetalleCreador_DeberiaRetornar404_CuandoNoEsCreadorNiAdmin() throws Exception {
        // Arrange: El usuario logueado es normal (USER) con ID 5
        usuarioAutenticadoMock.setRol(Roles.EQUIPO);
        ReflectionTestUtils.setField(usuarioAutenticadoMock, "id", 5L);;

        EventoEntidad entidad = new EventoEntidad();
        ReflectionTestUtils.setField(entidad, "id", 9L);

        // El evento fue creado por otro usuario (ID 99)
        UsuarioEntidad dueño = new UsuarioEntidad();
        ReflectionTestUtils.setField(dueño, "id", 99L);
        entidad.setCreador(dueño);

        when(consultarEventoUseCase.obtenerPorId(9L)).thenReturn(entidad);

        // Act & Assert
        // Tu código atrapa IllegalArgumentException y retorna 404 NOT_FOUND.
        mockMvc.perform(get("/api/eventos/9/creador"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Acceso Denegado: No tienes permiso para visualizar como creador un evento que no creaste."));
    }
}