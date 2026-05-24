package org.bormun.evento;

import org.bormun.aplicacion.repositorios.UsuarioRepository;
import org.bormun.infraestructura.configs.DataInitializer;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.junit.jupiter.api.BeforeEach; // NUEVO
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils; // NUEVO

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(dataInitializer, "adminEmail", "admin@gmail.com");
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "ClaveSimulada123");
    }

    @Test
    void run_NoDebeCrearAdmin_CuandoYaExiste() throws Exception {
        when(usuarioRepository.existsByEmail("admin@gmail.com")).thenReturn(true);

        dataInitializer.run();

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void run_DebeCrearAdmin_CuandoNoExiste() throws Exception {
        when(usuarioRepository.existsByEmail("admin@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash_simulado");

        dataInitializer.run();

        verify(usuarioRepository, times(1)).save(any(UsuarioEntidad.class));
    }
}