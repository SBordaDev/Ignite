package org.bormun.evento;

import org.bormun.aplicacion.repositorios.EventoRepository;
import org.bormun.aplicacion.usecase.ConsultarEvento;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarEventoTest {

    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private ConsultarEvento consultarEvento;

    @Test
    void obtenerPorId_DebeRetornarEvento_CuandoExiste() {
        // Arrange
        Long id = 1L;
        EventoEntidad eventoMock = new EventoEntidad();
        when(eventoRepository.findById(id)).thenReturn(Optional.of(eventoMock));

        // Act
        EventoEntidad resultado = consultarEvento.obtenerPorId(id);

        // Assert
        assertNotNull(resultado);
        verify(eventoRepository, times(1)).findById(id);
    }

    @Test
    void obtenerPorId_DebeLanzarExcepcion_CuandoNoExiste() {
        // Arrange
        Long id = 99L;
        when(eventoRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            consultarEvento.obtenerPorId(id);
        });

        assertEquals("El evento con el id (" + id + ") no fue encontrado", excepcion.getMessage());
        verify(eventoRepository, times(1)).findById(id);
    }

    @Test
    void listarAbiertos_DebeRetornarListaDeEventos() {
        // Arrange
        List<EventoEntidad> eventosAbiertos = List.of(new EventoEntidad(), new EventoEntidad());
        when(eventoRepository.findByInscripcionAbiertaTrue()).thenReturn(eventosAbiertos);

        // Act
        List<EventoEntidad> resultado = consultarEvento.listarAbiertos();

        // Assert
        assertEquals(2, resultado.size());
        verify(eventoRepository, times(1)).findByInscripcionAbiertaTrue();
    }
}