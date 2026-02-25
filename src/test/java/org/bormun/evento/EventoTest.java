package org.bormun.evento;

import org.bormun.domain.solicitud.EstadoSolicitud;
import org.bormun.domain.solicitud.Solicitud;
import org.bormun.domain.solicitud.SolicitudInvalidaException;
import org.bormun.domain.categoria.Categoria;
import org.bormun.domain.evento.Evento;
import org.bormun.domain.evento.GeneroNacimiento;
import org.bormun.domain.categoria.Restricciones;
import org.bormun.domain.participante.DatosDeportista;
import org.bormun.domain.participante.Equipo;
import org.bormun.usecase.AceptarSolicitud;
import org.bormun.usecase.EnviarSolicitud;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InscripcionesTest {
    private Evento evento;
    private EnviarSolicitud useCaseEnviar = new EnviarSolicitud();
    private AceptarSolicitud useCaseAceptar = new AceptarSolicitud();

    private final String CAT_MASCULINA = "Categoria Juevenil Masculina";
    private final String CAT_FEMENINA = "Categoria Sub30 Femenina";

    @BeforeEach
    void setup() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 13, 15, 30);

        Restricciones r1 = new Restricciones(12, 20, GeneroNacimiento.HOMBRE, 2, 2);
        Restricciones r2 = new Restricciones(30, 40, GeneroNacimiento.MUJER, 2, 3);

        this.evento = new Evento();
        evento.agregarCategoria(CAT_MASCULINA, 5000, r1);
        evento.agregarCategoria(CAT_FEMENINA, 5000, r2);
    }

    @Test
    @DisplayName("Las categorías deben inicializarse correctamente en el evento")
    void verificarCreacionCategorias() {
        assertAll("Atributos de categorías",
                () -> assertEquals(CAT_MASCULINA, evento.getCategorias().get(0).getNombreCategoria()),
                () -> assertEquals(CAT_FEMENINA, evento.getCategorias().get(1).getNombreCategoria())
        );
    }

    @Nested
    @DisplayName("Pruebas de Restricciones (Validación de fallos)")
    class RestriccionesFailTests {

        @Test
        @DisplayName("Falla cuando hay géneros mixtos o incorrectos")
        void testErrorGenero() {
            Equipo equipo = crearEquipoConDeportista("Carlos", GeneroNacimiento.HOMBRE, 2005);
            equipo.agregarIntegrante(new DatosDeportista("Maria", "345", GeneroNacimiento.MUJER, LocalDate.of(2006, 7, 14)));

            Solicitud solicitud = new Solicitud("Org1", equipo, evento.getCategorias().get(0));

            var ex = assertThrows(SolicitudInvalidaException.class, () -> useCaseEnviar.enviarSolicitud(evento, solicitud));
            assertEquals("Hay deportistas que no cumplen con los requisitos", ex.getMessage());
        }

        @Test
        @DisplayName("Falla cuando la edad está fuera de rango")
        void testErrorEdad() {
            Equipo equipo = crearEquipoConDeportista("Sofia", GeneroNacimiento.MUJER, 2005); // Demasiado joven para Cat Sub30

            Solicitud solicitud = new Solicitud("Org1", equipo, evento.getCategorias().get(1));

            var ex = assertThrows(SolicitudInvalidaException.class, () -> useCaseEnviar.enviarSolicitud(evento, solicitud));
            assertEquals("Hay deportistas que no cumplen con los requisitos", ex.getMessage());
            assertEquals(1, ex.getDeportistasCulpables().size());
            assertEquals("Sofia", ex.getDeportistasCulpables().get(0).getNombre());
        }

        @Test
        @DisplayName("Falla cuando el equipo excede el número de integrantes")
        void testErrorTamanoEquipo() {
            Equipo equipo = new Equipo("Equipo Grande");
            equipo.agregarIntegrante(List.of(
                    new DatosDeportista("D1", "1", GeneroNacimiento.HOMBRE, LocalDate.of(2010, 1, 1)),
                    new DatosDeportista("D2", "2", GeneroNacimiento.HOMBRE, LocalDate.of(2010, 1, 1)),
                    new DatosDeportista("D3", "3", GeneroNacimiento.HOMBRE, LocalDate.of(2010, 1, 1))
            ));

            Solicitud solicitud = new Solicitud("Org1", equipo, evento.getCategorias().get(0));

            var ex = assertThrows(SolicitudInvalidaException.class, () -> useCaseEnviar.enviarSolicitud(evento, solicitud));
            assertEquals("El Equipo es demasiado grande para el torneo", ex.getMessage());
        }

        @Test
        @DisplayName("Falla cuando la categoría ya alcanzó el máximo de equipos")
        void testErrorCategoriaLlena() {
            // Llenamos la capacidad (max 2 equipos según Restricciones r1)
            llenarCategoria(evento.getCategorias().get(0), 2);

            Equipo equipo3 = crearEquipoConDeportista("Culpable", GeneroNacimiento.HOMBRE, 2010);
            Solicitud solicitud3 = new Solicitud("Org3", equipo3, evento.getCategorias().get(0));

            var ex = assertThrows(SolicitudInvalidaException.class, () -> useCaseEnviar.enviarSolicitud(evento, solicitud3));
            assertEquals("Ya no se pueden inscribir mas equipos a esta categoria", ex.getMessage());
        }
    }

    @Test
    @DisplayName("El envío de solicitud exitoso debe registrarse correctamente")
    void verificarEnvioExitoso() {
        Equipo equipo = crearEquipoConDeportista("Carlos", GeneroNacimiento.HOMBRE, 2010);
        Solicitud solicitud = new Solicitud("OrgExito", equipo, evento.getCategorias().get(0));

        useCaseEnviar.enviarSolicitud(evento, solicitud);

        assertAll("Estado post-envío",
                () -> assertEquals(1, evento.getSolicitudes().size()),
                () -> assertEquals(EstadoSolicitud.EN_PROCESO, evento.getSolicitudes().get(0).getEstadoSolicitud()),
                () -> assertTrue(evento.getCategorias().get(0).getInscritos().isEmpty(), "No debe estar inscrito hasta ser aceptada")
        );
    }

    // --- MÉTODOS AUXILIARES (Para limpiar los tests) ---

    private Equipo crearEquipoConDeportista(String nombre, GeneroNacimiento genero, int anio) {
        Equipo equipo = new Equipo("Equipo " + nombre);
        equipo.agregarIntegrante(new DatosDeportista(nombre, "ID", genero, LocalDate.of(anio, 1, 1)));
        return equipo;
    }

    private void llenarCategoria(Categoria cat, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            cat.agregarEquipo(new Equipo("Equipo Llenado " + i));
        }
    }
}