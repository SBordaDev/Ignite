package org.bormun.evento;

import org.bormun.aplicacion.mapper.CategoriaMapper;
import org.bormun.aplicacion.mapper.SolicitudMapper;
import org.bormun.aplicacion.repositorios.EventoRepository;
import org.bormun.aplicacion.repositorios.SolicitudRepository;
import org.bormun.aplicacion.usecase.EnviarSolicitud;
import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Deportista;
import org.bormun.dominio.modelos.Equipo;
import org.bormun.dominio.modelos.Solicitud;
import org.bormun.infraestructura.entidades.CategoriaEntidad;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.entidades.SolicitudEntidad;
import org.bormun.presentacion.dto.request.SolicitudRequestDTO;
import org.bormun.presentacion.dto.request.EquipoRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnviarSolicitudTest {

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private SolicitudRepository solicitudRepository;

    @InjectMocks
    private EnviarSolicitud enviarSolicitud;

    private final Long eventoId = 1L;
    private EventoEntidad eventoEntidad;
    private CategoriaEntidad categoriaEntidad;
    private SolicitudRequestDTO dtoRequestMock;

    @BeforeEach
    void setUp() {
        eventoEntidad = new EventoEntidad();
        ReflectionTestUtils.setField(eventoEntidad, "id", eventoId);
        eventoEntidad.setInscripcionAbierta(true); // Abierto por defecto

        categoriaEntidad = new CategoriaEntidad();
        ReflectionTestUtils.setField(categoriaEntidad, "id", 10L);
        eventoEntidad.setCategorias(List.of(categoriaEntidad));

        // Usamos un mock del DTO (asumiendo que es un Record) para no tener que instanciar toda su estructura
        dtoRequestMock = mock(SolicitudRequestDTO.class);
    }

    @Test
    void ejecutar_DebeLanzarExcepcion_CuandoEventoNoExiste() {
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            enviarSolicitud.ejecutar(eventoId, dtoRequestMock);
        });
        assertEquals("Evento no encontrado", ex.getMessage());
    }

    @Test
    void ejecutar_DebeLanzarExcepcion_CuandoInscripcionesEstanCerradas() {
        eventoEntidad.setInscripcionAbierta(false);
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoEntidad));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            enviarSolicitud.ejecutar(eventoId, dtoRequestMock);
        });
        assertEquals("Las inscripciones para este evento están cerradas.", ex.getMessage());
    }

    @Test
    void ejecutar_DebeLanzarExcepcion_CuandoCategoriaNoPerteneceAlEvento() {
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoEntidad));
        when(dtoRequestMock.idCategoria()).thenReturn(99); // ID que no existe en el evento

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            enviarSolicitud.ejecutar(eventoId, dtoRequestMock);
        });
        assertEquals("La categoría no pertenece a este evento", ex.getMessage());
    }

    @Test
    void ejecutar_DebeProcesarYGuardarSolicitudExitosamente() {
        // Arrange
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(eventoEntidad));
        when(dtoRequestMock.idCategoria()).thenReturn(10); // ID que coincide
        when(dtoRequestMock.nombreOrganizacion()).thenReturn("Club Deportivo X");

        EquipoRequestDTO equipoRequestMock = mock(EquipoRequestDTO.class);
        when(dtoRequestMock.equipo()).thenReturn(equipoRequestMock);

        // Aislamiento de los mappers estáticos
        try (MockedStatic<CategoriaMapper> catMapperMock = mockStatic(CategoriaMapper.class);
             MockedStatic<SolicitudMapper> solMapperMock = mockStatic(SolicitudMapper.class)) {

            // Burlamos los modelos de dominio
            Categoria catDominioMock = mock(Categoria.class);
            Equipo equipoDominioMock = mock(Equipo.class);
            Deportista deportistaMock = mock(Deportista.class);

            // Configuramos el comportamiento de los mappers
            catMapperMock.when(() -> CategoriaMapper.aDominio(categoriaEntidad)).thenReturn(catDominioMock);
            solMapperMock.when(() -> SolicitudMapper.aDominio(equipoRequestMock)).thenReturn(equipoDominioMock);

            // Configuramos el dominio para que devuelva la lista de deportistas (para el for-loop) y el precio
            when(equipoDominioMock.getIntegrantes()).thenReturn(List.of(deportistaMock));
            when(catDominioMock.getPrecioInscripcion()).thenReturn(150000);

            // Burlamos el mapeo inverso de Dominio -> Entidad
            SolicitudEntidad solicitudEntidadResultante = new SolicitudEntidad();
            solMapperMock.when(() -> SolicitudMapper.aEntidad(any(Solicitud.class))).thenReturn(solicitudEntidadResultante);

            // Act
            enviarSolicitud.ejecutar(eventoId, dtoRequestMock);

            // Assert
            // 1. Verificamos que se invocaron las validaciones de negocio en el dominio
            verify(catDominioMock, times(1)).verificarDeportista(deportistaMock);
            verify(catDominioMock, times(1)).verificarEquipo(equipoDominioMock);

            // 2. Verificamos que se asignó la categoría a la entidad antes de guardar
            assertEquals(categoriaEntidad, solicitudEntidadResultante.getCategoria());

            // 3. Verificamos que se llamó al repositorio para guardar
            verify(solicitudRepository, times(1)).save(solicitudEntidadResultante);
        }
    }
}