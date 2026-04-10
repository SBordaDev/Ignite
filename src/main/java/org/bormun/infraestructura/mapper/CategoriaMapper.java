package org.bormun.infraestructura.mapper;

import org.bormun.aplicacion.dto.request.DeportistaRequestDTO;
import org.bormun.aplicacion.dto.request.EquipoRequestDTO;
import org.bormun.aplicacion.dto.request.RestriccionesRequestDTO;
import org.bormun.aplicacion.dto.response.CategoriaResponseDTO;
import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Equipo;
import org.bormun.dominio.modelos.Restricciones;
import org.bormun.dominio.modelos.Solicitud;
import org.bormun.infraestructura.entidades.*;

import java.util.ArrayList;
import java.util.List;

public class CategoriaMapper {
    public static CategoriaEntidad aEntidad(Categoria categoria, List<Solicitud> solDelCategoria){
        CategoriaEntidad entidad = new CategoriaEntidad();
        List<SolicitudEntidad> solicitudes = new ArrayList<>();
        List<EquipoEntidad> inscritos = new ArrayList<>();

        entidad.setId(categoria.getId());
        entidad.setNombreCategoria(categoria.getNombreCategoria());
        entidad.setPrecioInscripcion(categoria.getPrecioInscripcion());

        for (Solicitud solicitud: solDelCategoria){
            SolicitudEntidad solicitudTraducida = SolicitudMapper.aEntidad(solicitud);
            solicitudTraducida.setCategoria(entidad);

            solicitudes.add(solicitudTraducida);
        }
        entidad.setSolicitudes(solicitudes);

        for (Equipo equipo: categoria.getInscritos()){
            inscritos.add(SolicitudMapper.aEntidad(equipo));
        }
        entidad.setInscritos(inscritos);


        entidad.setRestricciones(aEntidad(categoria.getRestricciones()));

        return entidad;
    }

    public static RestriccionesEntidad aEntidad(Restricciones restricciones){
        RestriccionesEntidad entidad = new RestriccionesEntidad();

        entidad.setEdadMaxima(restricciones.edadMaxima());
        entidad.setEdadMinima(restricciones.edadMinima());
        entidad.setGeneroNacimiento(restricciones.generoNacimiento());
        entidad.setNumeroEquipo(restricciones.numeroEquipo());
        entidad.setNumeroIntegrantesPorEquipo(restricciones.numeroIntegrantesPorEquipo());

        return entidad;
    }

    public static Categoria aDominio(CategoriaEntidad categoria){
        Categoria dominio = new Categoria(
                categoria.getNombreCategoria(),
                categoria.getPrecioInscripcion(),
                aDominio(categoria.getRestricciones())
        );
        dominio.setId(categoria.getId());
        for (EquipoEntidad equipo: categoria.getInscritos()){
            dominio.agregarEquipo(SolicitudMapper.aDominio(equipo));
        }
        return dominio;
    }

    public static Restricciones aDominio(RestriccionesEntidad restricciones){
        return new Restricciones(
                restricciones.getEdadMinima(),
                restricciones.getEdadMaxima(),
                restricciones.getGeneroNacimiento(),
                restricciones.getNumeroEquipo(),
                restricciones.getNumeroIntegrantesPorEquipo()
        );
    }

    public static CategoriaResponseDTO aCategoriaResponseDTO(CategoriaEntidad entidad){
        List<EquipoRequestDTO> equipos = new ArrayList<>();
        for (EquipoEntidad equipo: entidad.getInscritos()){
            equipos.add(aEquipoRequest(equipo));
        }

        return new CategoriaResponseDTO(
                entidad.getId(),
                entidad.getNombreCategoria(),
                entidad.getPrecioInscripcion(),
                equipos,
                new RestriccionesRequestDTO(
                        entidad.getRestricciones().getEdadMinima(),
                        entidad.getRestricciones().getEdadMaxima(),
                        entidad.getRestricciones().getGeneroNacimiento(),
                        entidad.getRestricciones().getNumeroEquipo(),
                        entidad.getRestricciones().getNumeroIntegrantesPorEquipo()
                )
        );
    }

    protected static EquipoRequestDTO aEquipoRequest(EquipoEntidad entidad){
        List<DeportistaRequestDTO> deportistas = new ArrayList<>();
        for(DeportistaEntidad deportista: entidad.getDeportistas()){
            deportistas.add(aDeportistaRequest(deportista));
        }
        return new EquipoRequestDTO(
                entidad.getNombreEquipo(),
                deportistas
        );
    }

    private static DeportistaRequestDTO aDeportistaRequest(DeportistaEntidad deportista){
        return new DeportistaRequestDTO(
                deportista.getNombre(),
                Long.parseLong(deportista.getIdentificacion()),
                deportista.getGeneroNacimiento(),
                deportista.getFechaNacimiento()
        );
    }
}
