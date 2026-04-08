package org.bormun.evento;

import org.bormun.dominio.excepciones.ErrorDeportista;
import org.bormun.dominio.modelos.*;
import org.bormun.dominio.excepciones.SolicitudInvalidaException;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InscripcionesTest {
    private Evento evento;

    private final String CAT_MASCULINA = "Categoria Juevenil Masculina";
    private final String CAT_FEMENINA = "Categoria Sub30 Femenina";

    @BeforeEach
    void setup() {
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 13, 15, 30);

        Restricciones r1 = new Restricciones(12, 20, GeneroNacimiento.HOMBRE, 2, 2);
        Restricciones r2 = new Restricciones(30, 40, GeneroNacimiento.MUJER, 2, 3);

        this.evento = new Evento("Evento 1");
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
            Equipo equipo = crearEquipoConDeportista("Carlos", GeneroNacimiento.HOMBRE, 2006);
            equipo.agregarIntegrante(new DatosDeportista("Maria", "345", GeneroNacimiento.MUJER, LocalDate.of(2006, 1, 1)));

            Solicitud solicitud = new Solicitud("Org1", equipo, evento.getCategorias().get(0));

            SolicitudInvalidaException ex = assertThrows(SolicitudInvalidaException.class, () -> enviarSolicitud(evento, solicitud));
            assertAll("Estado error",
                    () -> assertEquals("Deportistas no cumplen con los requisitos", ex.getMessage()),
                    () -> assertEquals(MotivoErrorDeportista.GENERO_INVALIDO, ex.getCulpables().get(0).getMotivoError()),
                    () -> assertEquals("Maria", ex.getCulpables().get(0).getDeportista().getNombre())
            );
        }

        @Test
        @DisplayName("Falla cuando la edad está fuera de rango")
        void testErrorEdad() {
            Equipo equipo = crearEquipoConDeportista("Sofia", GeneroNacimiento.MUJER, 2005); // Demasiado joven para Cat Sub30

            Solicitud solicitud = new Solicitud("Org1", equipo, evento.getCategorias().get(1));

            SolicitudInvalidaException ex = assertThrows(SolicitudInvalidaException.class, () -> enviarSolicitud(evento, solicitud));
            assertAll("Estado error",
                    () -> assertEquals("Deportistas no cumplen con los requisitos", ex.getMessage()),
                    () -> assertEquals(1, ex.getCulpables().size()),
                    () -> assertEquals("Sofia", ex.getCulpables().get(0).getDeportista().getNombre()),
                    () -> assertEquals(MotivoErrorDeportista.EDAD_INVALIDA, ex.getCulpables().get(0).getMotivoError())
            );

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

            var ex = assertThrows(SolicitudInvalidaException.class, () -> enviarSolicitud(evento, solicitud));
            assertEquals("El Equipo es demasiado grande para el torneo", ex.getMessage());
        }

        @Test
        @DisplayName("Falla cuando la categoría ya alcanzó el máximo de equipos")
        void testErrorCategoriaLlena() {
            // Llenamos la capacidad (max 2 equipos según Restricciones r1)
            llenarCategoria(evento.getCategorias().get(0), 2);

            Equipo equipo3 = crearEquipoConDeportista("Culpable", GeneroNacimiento.HOMBRE, 2010);
            Solicitud solicitud3 = new Solicitud("Org3", equipo3, evento.getCategorias().get(0));

            var ex = assertThrows(SolicitudInvalidaException.class, () -> enviarSolicitud(evento, solicitud3));
            assertEquals("Ya no se pueden inscribir mas equipos a esta categoria", ex.getMessage());
        }
    }

    @Test
    @DisplayName("El envío de solicitud exitoso debe registrarse correctamente")
    void verificarEnvioExitoso() {
        Equipo equipo = crearEquipoConDeportista("Carlos", GeneroNacimiento.HOMBRE, 2010);
        Solicitud solicitud = new Solicitud("OrgExito", equipo, evento.getCategorias().get(0));

        enviarSolicitud(evento, solicitud);

        assertAll("Estado post-envío",
                () -> assertEquals(1, evento.getSolicitudes().size()),
                () -> assertEquals(EstadoSolicitud.EN_PROCESO, evento.getSolicitudes().get(0).getEstadoSolicitud()),
                () -> assertTrue(evento.getCategorias().get(0).getInscritos().isEmpty(), "No debe estar inscrito hasta ser aceptada")
        );
    }

    @Test
    @DisplayName("Puedo aceptar solicitudes")
    void aceptarSolicitud(){
        Equipo equipo = crearEquipoConDeportista("Carlos", GeneroNacimiento.HOMBRE, 2010);
        Solicitud solicitud = new Solicitud("OrgExito", equipo, evento.getCategorias().get(0));

        enviarSolicitud(evento, solicitud);

        aceptarSolicitud(solicitud);
        assertAll("Estado post-aceptado",
                () -> assertEquals(1, evento.getCategorias().get(0).getInscritos().size()),
                () -> assertEquals(EstadoSolicitud.ACEPTADO, solicitud.getEstadoSolicitud())
        );
    }

    @Test
    @DisplayName("Puedo negar solicitudes")
    void negarSolicitud(){
        Equipo equipo = crearEquipoConDeportista("Carlos", GeneroNacimiento.HOMBRE, 2010);
        Solicitud solicitud = new Solicitud("OrgExito", equipo, evento.getCategorias().get(0));

        enviarSolicitud(evento, solicitud);

        negarSolicitud(solicitud, "porque si");
        assertAll("Estado post-negado",
                () -> assertTrue(evento.getCategorias().get(0).getInscritos().isEmpty(), "La categoria esta vacia porque se nego el acceso"),
                () -> assertEquals(EstadoSolicitud.RECHAZADO, solicitud.getEstadoSolicitud())
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

    private void negarSolicitud(Solicitud solicitud, String comenterio){
        solicitud.setEstadoSolicitud(EstadoSolicitud.RECHAZADO);
        solicitud.setComentarios(comenterio);
    }

    private void aceptarSolicitud(Solicitud solicitud){
        solicitud.setEstadoSolicitud(EstadoSolicitud.ACEPTADO);
        solicitud.getCategoria().agregarEquipo(solicitud.getEquipo());
    }

    public void enviarSolicitud(Evento evento, Solicitud solicitud){

        if(!evento.isInscripcionAbierta()){
            throw new SolicitudInvalidaException("Las inscripciones estan cerradas");
        }

        Categoria categoriaReal = evento.getCategorias().stream()
                .filter(c -> c.getNombreCategoria().equals(solicitud.getCategoria().getNombreCategoria()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La categoría no existe"));

        Equipo equipo = solicitud.getEquipo();

        List<ErrorDeportista> conErrores = new ArrayList<>();

        for (Deportista deportista : equipo.getIntegrantes()) {
            try{
                categoriaReal.verificarDeportista(deportista);
            } catch (ErrorDeportista e) {
                conErrores.add(e);
            }
        }

        if(!conErrores.isEmpty()){
            throw new SolicitudInvalidaException("Deportistas no cumplen con los requisitos", conErrores);
        }

        categoriaReal.verificarEquipo(equipo);

        Solicitud solicitudValida = new Solicitud(
                solicitud.getNombreOrganizacion(),
                equipo,
                categoriaReal
        );


        solicitudValida.actualizarPrecioTotal(categoriaReal.getPrecioInscripcion());
        evento.agregarSolicitud(solicitudValida);

    }
}