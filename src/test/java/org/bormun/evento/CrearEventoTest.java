package org.bormun.evento;

import org.bormun.aplicacion.mapper.EventoMapper;
import org.bormun.aplicacion.repositorios.EventoRepository;
import org.bormun.aplicacion.usecase.CrearEvento;
import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Evento;
import org.bormun.dominio.modelos.GeneroNacimiento;
import org.bormun.dominio.modelos.Restricciones;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrearEventoTest {

    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private CrearEvento crearEvento;

    @Test
    void crearEvento_DebeLanzarExcepcion_CuandoNombreEsNulo() {
        Evento evento = new Evento(null);
        UsuarioEntidad creador = new UsuarioEntidad();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> crearEvento.crearEvento(evento, creador));
        assertEquals("El evento no puede crearse sin un nombre válido.", ex.getMessage());
    }

    @Test
    void crearEvento_DebeLanzarExcepcion_CuandoNombreEsVacio() {
        Evento evento = new Evento("   ");
        UsuarioEntidad creador = new UsuarioEntidad();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> crearEvento.crearEvento(evento, creador));
        assertEquals("El evento no puede crearse sin un nombre válido.", ex.getMessage());
    }

    @Test
    void crearEvento_DebeLanzarExcepcion_CuandoCategoriasSonNulas() {
        Evento evento = new Evento("Torneo Relámpago");
        evento.setCategorias(null);
        UsuarioEntidad creador = new UsuarioEntidad();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> crearEvento.crearEvento(evento, creador));
        assertEquals("El evento debe tener al menos una categoría para ser creado.", ex.getMessage());
    }

    @Test
    void crearEvento_DebeLanzarExcepcion_CuandoCategoriasEstanVacias() {
        Evento evento = new Evento("Torneo Relámpago");
        evento.setCategorias(List.of());
        UsuarioEntidad creador = new UsuarioEntidad();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> crearEvento.crearEvento(evento, creador));
        assertEquals("El evento debe tener al menos una categoría para ser creado.", ex.getMessage());
    }

    @Test
    void crearEvento_DebeLanzarExcepcion_CuandoNombreYaExiste() {
        Evento evento = new Evento("Torneo Relámpago");
        evento.setCategorias(List.of(new Categoria(
                "A1",
                1000,
                new Restricciones(
                        5,
                        10,
                        GeneroNacimiento.HOMBRE,
                        4,
                        5
                )
        )));
        UsuarioEntidad creador = new UsuarioEntidad();

        when(eventoRepository.existsByNombreIgnoreCase("Torneo Relámpago")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> crearEvento.crearEvento(evento, creador));
        assertEquals("El nombre del evento ya existe", ex.getMessage());
    }

    @Test
    void crearEvento_DebeGuardarYRetornarEvento_CuandoTodoEsValido() {
        // Arrange
        Evento eventoNuevo = new Evento("Torneo Relámpago");
        eventoNuevo.setCategorias(List.of(new Categoria(
                "A1",
                1000,
                new Restricciones(
                        5,
                        10,
                        GeneroNacimiento.HOMBRE,
                        4,
                        5
                )
        )));
        UsuarioEntidad creador = new UsuarioEntidad();

        when(eventoRepository.existsByNombreIgnoreCase("Torneo Relámpago")).thenReturn(false);

        EventoEntidad entidadGuardada = new EventoEntidad();
        when(eventoRepository.save(any(EventoEntidad.class))).thenReturn(entidadGuardada);

        // Usamos try-with-resources para burlar de manera segura los métodos estáticos del Mapper
        try (MockedStatic<EventoMapper> mapperMock = mockStatic(EventoMapper.class)) {
            EventoEntidad entidadMapeada = new EventoEntidad();
            mapperMock.when(() -> EventoMapper.aEntidad(eventoNuevo)).thenReturn(entidadMapeada);

            Evento eventoFinal = new Evento("EventoFinal");
            mapperMock.when(() -> EventoMapper.aDominio(entidadGuardada)).thenReturn(eventoFinal);

            // Act
            Evento resultado = crearEvento.crearEvento(eventoNuevo, creador);

            // Assert
            assertNotNull(resultado);
            assertEquals(eventoFinal, resultado);
            assertEquals(creador, entidadMapeada.getCreador()); // Validar que se asignó el creador
            verify(eventoRepository, times(1)).save(entidadMapeada);
        }
    }
}
