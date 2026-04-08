package org.bormun.evento;

import org.bormun.dominio.modelos.*;
import org.bormun.infraestructura.entidades.*;
import org.bormun.infraestructura.mapper.EventoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapperEventoTest {
    private final Evento evento = new Evento("Evento1");
    private Solicitud solicitud;

    @BeforeEach
    void setup() {
        Restricciones r1 = new Restricciones(12, 20, GeneroNacimiento.HOMBRE, 2, 2);
        Restricciones r2 = new Restricciones(30, 40, GeneroNacimiento.MUJER, 2, 3);

        evento.agregarCategoria("CAT_MASCULINA", 5000, r1);
        evento.agregarCategoria("CAT_FEMENINA", 5000, r2);

        Equipo equipo = new Equipo("team 1 IG");
        equipo.agregarIntegrante(new DatosDeportista("Carlos", "345", GeneroNacimiento.HOMBRE, LocalDate.of(2006, 1, 1)));

        solicitud = new Solicitud("IGNITE ELITE TEAM", equipo, evento.getCategorias().get(0));

        // ¡Agregamos el equipo y la solicitud al dominio para que el Mapper pueda leerlos!
        evento.getCategorias().get(0).agregarEquipo(equipo);
        evento.agregarSolicitud(solicitud);
    }

    @Test
    @DisplayName("El Mapper traduce el evento y preserva las instancias en memoria")
    void verificarCreacionEntidades(){
        // 1. Ejecutamos tu Mapper
        EventoEntidad entidad = EventoMapper.aEntidad(evento);

        // 2. Verificamos el Evento base
        assertNotNull(entidad, "La entidad generada no debe ser nula");
        assertEquals("Evento1", entidad.getNombre(), "El nombre del evento debe coincidir");

        // 3. Verificamos las Categorías
        List<CategoriaEntidad> categorias = entidad.getCategorias();
        assertEquals(2, categorias.size(), "Debe haber 2 categorías traducidas");
        assertEquals("CAT_MASCULINA", categorias.get(0).getNombreCategoria());

        // 4. Verificamos las Solicitudes
        List<SolicitudEntidad> solicitudes = entidad.getCategorias().get(0).getSolicitudes();
        assertEquals(1, solicitudes.size(), "Debe haber 1 solicitud traducida");

        SolicitudEntidad solEntidad = solicitudes.get(0);
        assertEquals("IGNITE ELITE TEAM", solEntidad.getNombreOrganizacion());

        // 5. ¡LA PRUEBA DE FUEGO DEL MAPPER! (Verificar que no hay clones)
        CategoriaEntidad catEnlazada = solEntidad.getCategoria();
        assertNotNull(catEnlazada, "La solicitud debe haber enlazado una categoría");

        // assertSame verifica que ambas variables apunten exactamente a la misma posición en RAM
        assertSame(categorias.get(0), catEnlazada, "¡Error! La solicitud creó un clon de la categoría en lugar de usar la misma instancia");

        EquipoEntidad equipoEnlazado = solEntidad.getEquipo();
        assertNotNull(equipoEnlazado, "La solicitud debe tener su equipo enlazado");
        assertEquals("team 1 IG", equipoEnlazado.getNombreEquipo());

        // 6. Verificamos la cascada hasta el deportista
        assertEquals(1, equipoEnlazado.getDeportistas().size(), "El equipo debe tener 1 integrante");
        assertEquals("Carlos", equipoEnlazado.getDeportistas().get(0).getNombre(), "El nombre del deportista debe ser Carlos");
    }
}