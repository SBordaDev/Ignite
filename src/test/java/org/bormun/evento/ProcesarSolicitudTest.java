package org.bormun.evento;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.bormun.aplicacion.mapper.CategoriaMapper;
import org.bormun.aplicacion.mapper.SolicitudMapper;
import org.bormun.aplicacion.repositorios.EventoRepository;
import org.bormun.aplicacion.repositorios.SolicitudRepository;
import org.bormun.aplicacion.usecase.ProcesarSolicitud;
import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Equipo;
import org.bormun.dominio.modelos.EstadoSolicitud;
import org.bormun.dominio.modelos.Roles;
import org.bormun.infraestructura.entidades.*;
import org.bormun.presentacion.dto.request.ProcesarSolicitudDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcesarSolicitudTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    // Registro en memoria real para las métricas de Micrometer
    private final MeterRegistry meterRegistry = new SimpleMeterRegistry();

    private ProcesarSolicitud procesarSolicitud;

    private UsuarioEntidad usuarioAutenticado;
    private SolicitudEntidad solicitud;
    private EventoEntidad evento;
    private ProcesarSolicitudDTO data;

    @BeforeEach
    void setUp() {
        procesarSolicitud = new ProcesarSolicitud(eventoRepository, solicitudRepository, meterRegistry);

        // Simulamos el usuario que hace la petición (Viene del token/DB)
        usuarioAutenticado = new UsuarioEntidad();
        ReflectionTestUtils.setField(usuarioAutenticado, "id", 1L); // Inyección por reflexión
        usuarioAutenticado.setRol(Roles.ADMIN);

        // Simulamos la solicitud que se recupera de la DB
        solicitud = new SolicitudEntidad();
        ReflectionTestUtils.setField(solicitud, "id", 1L); // Inyección por reflexión
        solicitud.setEstadoSolicitud(EstadoSolicitud.EN_PROCESO);

        // Simulamos el creador del evento y el evento recuperado de la DB
        UsuarioEntidad creadorEvento = new UsuarioEntidad();
        ReflectionTestUtils.setField(creadorEvento, "id", 2L); // Diferente al usuario autenticado por defecto

        evento = new EventoEntidad();
        ReflectionTestUtils.setField(evento, "id", 1L);
        evento.setCreador(creadorEvento);

        data = new ProcesarSolicitudDTO(EstadoSolicitud.ACEPTADO, "Aprobado por el admin");
    }

    @Test
    void debeLanzarExcepcion_CuandoSolicitudNoExiste() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> procesarSolicitud.ejecutar(1L, data, usuarioAutenticado));
        assertEquals("La solicitud con id (1) no fue encontrada", ex.getMessage());
    }

    @Test
    void debeLanzarExcepcion_CuandoEventoNoExiste() {
        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(eventoRepository.findEventoBySolicitudId(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> procesarSolicitud.ejecutar(1L, data, usuarioAutenticado));
        assertEquals("Evento no encontrado para esta solicitud", ex.getMessage());
    }

    @Test
    void debeLanzarExcepcion_CuandoUsuarioNoEsAdminNiCreador() {
        usuarioAutenticado.setRol(Roles.EQUIPO); // No es admin
        ReflectionTestUtils.setField(usuarioAutenticado, "id", 99L); // No es el creador (el creador tiene ID 2)

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(eventoRepository.findEventoBySolicitudId(1L)).thenReturn(Optional.of(evento));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> procesarSolicitud.ejecutar(1L, data, usuarioAutenticado));
        assertEquals("Acceso Denegado: No tienes permiso para modificar un evento que no creaste.", ex.getMessage());
    }

    @Test
    void debeLanzarExcepcion_CuandoSolicitudYaNoEstaEnProceso() {
        solicitud.setEstadoSolicitud(EstadoSolicitud.ACEPTADO); // Simulamos que ya fue procesada previamente en la DB

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(eventoRepository.findEventoBySolicitudId(1L)).thenReturn(Optional.of(evento));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> procesarSolicitud.ejecutar(1L, data, usuarioAutenticado));
        assertEquals("Esta solicitud ya fue procesada previamente y no puede ser modificada.", ex.getMessage());
    }

    @Test
    void debeProcesarExitosamente_YGuardarComentarios_CuandoEsRechazado() {
        ProcesarSolicitudDTO dataRechazo = new ProcesarSolicitudDTO(EstadoSolicitud.RECHAZADO, "Faltan documentos");

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(eventoRepository.findEventoBySolicitudId(1L)).thenReturn(Optional.of(evento));

        procesarSolicitud.ejecutar(1L, dataRechazo, usuarioAutenticado);

        assertEquals(EstadoSolicitud.RECHAZADO, solicitud.getEstadoSolicitud());
        assertEquals("Faltan documentos", solicitud.getComentarios());
        verify(solicitudRepository, times(1)).save(solicitud);
        assertEquals(1.0, meterRegistry.counter("ignite.solicitudes.procesadas", "estado_final", "RECHAZADO").count());
    }

    @Test
    void debeProcesarExitosamente_IgnorarComentariosVacios_YVerificarEquipo_CuandoEsAceptado() {
        ProcesarSolicitudDTO dataAceptadoSinComentario = new ProcesarSolicitudDTO(EstadoSolicitud.ACEPTADO, "   ");

        CategoriaEntidad categoriaEntidad = new CategoriaEntidad();
        categoriaEntidad.setInscritos(new ArrayList<>());
        EquipoEntidad equipoEntidad = new EquipoEntidad();

        solicitud.setCategoria(categoriaEntidad);
        solicitud.setEquipo(equipoEntidad);

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));

        // Simulamos que el usuario logueado no es admin, pero sí es el creador exacto del evento
        usuarioAutenticado.setRol(Roles.CREADOR);
        ReflectionTestUtils.setField(usuarioAutenticado, "id", 2L);
        when(eventoRepository.findEventoBySolicitudId(1L)).thenReturn(Optional.of(evento));

        // Aislamiento de los métodos estáticos de mapeo
        try (MockedStatic<CategoriaMapper> catMapperMock = mockStatic(CategoriaMapper.class);
             MockedStatic<SolicitudMapper> solMapperMock = mockStatic(SolicitudMapper.class)) {

            Categoria catDominioMock = mock(Categoria.class);
            Equipo equipoDominioMock = mock(Equipo.class);

            catMapperMock.when(() -> CategoriaMapper.aDominio(categoriaEntidad)).thenReturn(catDominioMock);
            solMapperMock.when(() -> SolicitudMapper.aDominio(equipoEntidad)).thenReturn(equipoDominioMock);

            procesarSolicitud.ejecutar(1L, dataAceptadoSinComentario, usuarioAutenticado);

            verify(catDominioMock).verificarEquipo(equipoDominioMock);
            assertTrue(categoriaEntidad.getInscritos().contains(equipoEntidad));

            assertEquals(EstadoSolicitud.ACEPTADO, solicitud.getEstadoSolicitud());
            assertNull(solicitud.getComentarios());

            verify(solicitudRepository, times(1)).save(solicitud);
            assertEquals(1.0, meterRegistry.counter("ignite.solicitudes.procesadas", "estado_final", "ACEPTADO").count());
        }
    }
}