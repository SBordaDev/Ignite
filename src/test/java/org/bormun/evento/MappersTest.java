package org.bormun.evento;

import org.bormun.aplicacion.mapper.CategoriaMapper;
import org.bormun.aplicacion.mapper.EventoMapper;
import org.bormun.aplicacion.mapper.SolicitudMapper;
import org.bormun.dominio.modelos.*;
import org.bormun.infraestructura.entidades.*;
import org.bormun.presentacion.dto.request.DeportistaRequestDTO;
import org.bormun.presentacion.dto.request.EquipoRequestDTO;
import org.bormun.presentacion.dto.response.CategoriaResponseDTO;
import org.bormun.presentacion.dto.response.EventoDetallePublicoDTO;
import org.bormun.presentacion.dto.response.EventoDetallesCreadorDTO;
import org.bormun.presentacion.dto.response.SolicitudResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Suite unificada de pruebas para Mappers")
class MappersTest {

    @Nested
    @DisplayName("Pruebas para CategoriaMapper")
    class CategoriaMapperTests {

        @Test
        void aEntidad_DeberiaMapearCorrectamente_CuandoDatosSonValidos() {
            // Arrange
            Restricciones restricciones = new Restricciones(18, 50, GeneroNacimiento.HOMBRE, 10, 5);
            Categoria categoria = new Categoria("Fútbol Masculino", 150000, restricciones);
            categoria.setId(1L);
            List<Solicitud> solicitudes = new ArrayList<>();

            // Act
            CategoriaEntidad resultado = CategoriaMapper.aEntidad(categoria, solicitudes);

            // Assert
            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
            assertEquals("Fútbol Masculino", resultado.getNombreCategoria());
            assertEquals(150000.0, resultado.getPrecioInscripcion());
            assertNotNull(resultado.getRestricciones());
            assertEquals(18, resultado.getRestricciones().getEdadMinima());
        }

        @Test
        void aCategoriaResponseDTO_DeberiaMapearEntidadADTO_ConSublistas() {
            // Arrange
            CategoriaEntidad entidad = new CategoriaEntidad();
            entidad.setId(2L);
            entidad.setNombreCategoria("Baloncesto Femenino");
            entidad.setPrecioInscripcion(120000);

            RestriccionesEntidad restEntidad = new RestriccionesEntidad();
            restEntidad.setEdadMinima(15);
            restEntidad.setEdadMaxima(35);
            restEntidad.setGeneroNacimiento(GeneroNacimiento.MUJER);
            entidad.setRestricciones(restEntidad);

            EquipoEntidad equipo = new EquipoEntidad();
            equipo.setNombreEquipo("Alpha Team");

            DeportistaEntidad deportista = new DeportistaEntidad();
            deportista.setNombre("Clara");
            deportista.setIdentificacion("123456");
            deportista.setGeneroNacimiento(GeneroNacimiento.MUJER);
            deportista.setFechaNacimiento(LocalDate.of(2000, 1, 1));

            equipo.setDeportistas(List.of(deportista));
            entidad.setInscritos(List.of(equipo));

            // Act
            CategoriaResponseDTO dto = CategoriaMapper.aCategoriaResponseDTO(entidad);

            // Assert
            assertNotNull(dto);
            assertEquals(2L, dto.id());
            assertEquals("Baloncesto Femenino", dto.nombreCategoria());
            assertEquals(1, dto.inscritos().size());
            assertEquals("Alpha Team", dto.inscritos().get(0).nombreEquipo());
            assertEquals(123456L, dto.inscritos().get(0).integrantes().get(0).identificacion());
        }
    }

    @Nested
    @DisplayName("Pruebas para EventoMapper")
    class EventoMapperTests {

        @Test
        void aEntidad_DeberiaConvertirDominioAEntidad() {
            // Arrange
            Evento evento = new Evento("Torneo Interfacultades");
            evento.setId(10L);
            evento.setInscripcionAbierta(true);

            // Act
            EventoEntidad resultado = EventoMapper.aEntidad(evento);

            // Assert
            assertNotNull(resultado);
            assertEquals(10L, resultado.getId());
            assertEquals("Torneo Interfacultades", resultado.getNombre());
            assertTrue(resultado.isInscripcionAbierta());
        }

        @Test
        void aDetallePublicoDTO_DeberiaMapearCamposPublicos() {
            // Arrange
            EventoEntidad entidad = new EventoEntidad();
            entidad.setId(5L);
            entidad.setNombre("Copa Sabana");
            entidad.setInscripcionAbierta(false);

            CategoriaEntidad cat = new CategoriaEntidad();
            cat.setRestricciones(new RestriccionesEntidad());
            entidad.setCategorias(List.of(cat));

            // Act
            EventoDetallePublicoDTO dto = EventoMapper.aDetallePublicoDTO(entidad);

            // Assert
            assertNotNull(dto);
            assertEquals(5L, dto.id());
            assertEquals("Copa Sabana", dto.nombre());
            assertFalse(dto.inscripcionAbierta());
        }

        @Test
        void aDetalleCreadorDTO_DeberiaFiltrarSolicitudesPorEstado() {
            // Arrange
            EventoEntidad entidad = new EventoEntidad();
            entidad.setId(12L);
            entidad.setNombre("Mega Evento");

            CategoriaEntidad cat = new CategoriaEntidad();
            cat.setRestricciones(new RestriccionesEntidad());
            cat.setInscritos(new ArrayList<>());
            cat.setSolicitudes(new ArrayList<>());
            entidad.setCategorias(List.of(cat));

            // Act
            EventoDetallesCreadorDTO dto = EventoMapper.aDetalleCreadorDTO(entidad, EstadoSolicitud.EN_PROCESO);

            // Assert
            assertNotNull(dto);
            assertEquals(12L, dto.id());
            assertNotNull(dto.solicitudes());
        }
    }

    @Nested
    @DisplayName("Pruebas para SolicitudMapper")
    class SolicitudMapperTests {

        @Test
        void aDominio_DeberiaMapearEquipoRequestADominio() {
            // Arrange
            DeportistaRequestDTO deportistaDTO = new DeportistaRequestDTO(
                    "Samuel Borda",
                    100200300L,
                    GeneroNacimiento.HOMBRE,
                    LocalDate.of(2005, 5, 19)
            );
            EquipoRequestDTO equipoDTO = new EquipoRequestDTO("Ingeniería FC", List.of(deportistaDTO));

            // Act
            Equipo dominio = SolicitudMapper.aDominio(equipoDTO);

            // Assert
            assertNotNull(dominio);
            assertEquals("Ingeniería FC", dominio.getNombreEquipo());
            assertEquals(1, dominio.getIntegrantes().size());
            assertEquals("Samuel Borda", dominio.getIntegrantes().get(0).getNombre());
            assertEquals("100200300", dominio.getIntegrantes().get(0).getId());
        }

        @Test
        void aDTO_DeberiaMapearEntidadADTO_Completamente() {
            // Arrange
            SolicitudEntidad entidad = new SolicitudEntidad();
            entidad.setId(50L);
            entidad.setNombreOrganizacion("Unisabana");
            entidad.setPagoConfirmado(true);
            entidad.setPrecioTotal(250000);

            CategoriaEntidad cat = new CategoriaEntidad();
            cat.setId(7L);
            cat.setNombreCategoria("Avanzados");
            entidad.setCategoria(cat);

            EquipoEntidad equipo = new EquipoEntidad();
            equipo.setNombreEquipo("Meteoro");
            equipo.setDeportistas(List.of());
            entidad.setEquipo(equipo);

            // Act
            SolicitudResponseDTO dto = SolicitudMapper.aDTO(entidad);

            // Assert
            assertNotNull(dto);
            assertEquals(50L, dto.id());
            assertEquals("Unisabana", dto.nombreOrganizacion());
            assertTrue(dto.pagoConfirmado());
            assertEquals(7L, dto.idCategoria());
            assertEquals("Avanzados", dto.nombreCategoria());
        }
    }
}