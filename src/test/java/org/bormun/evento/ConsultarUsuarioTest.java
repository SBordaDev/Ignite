package org.bormun.evento;

import org.bormun.aplicacion.repositorios.UsuarioRepository;
import org.bormun.aplicacion.usecase.ConsultarUsuario;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultarUsuarioTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ConsultarUsuario consultarUsuario;

    @Test
    void buscarPorId_DebeRetornarUsuario_CuandoExiste() {
        // Arrange
        Long id = 1L;
        UsuarioEntidad usuarioMock = new UsuarioEntidad();
        ReflectionTestUtils.setField(usuarioMock, "id", id);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuarioMock));

        // Act
        UsuarioEntidad resultado = consultarUsuario.buscarPorId(id);

        // Assert
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        verify(usuarioRepository, times(1)).findById(id);
    }

    @Test
    void buscarPorId_DebeLanzarExcepcion_CuandoNoExiste() {
        // Arrange
        Long id = 99L;
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            consultarUsuario.buscarPorId(id);
        });

        assertEquals("No se encontro el usuario con la id (" + id + ")", excepcion.getMessage());
        verify(usuarioRepository, times(1)).findById(id);
    }
}
