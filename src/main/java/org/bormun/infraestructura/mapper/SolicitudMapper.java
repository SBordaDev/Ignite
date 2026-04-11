package org.bormun.infraestructura.mapper;

import org.bormun.aplicacion.dto.request.DeportistaRequestDTO;
import org.bormun.aplicacion.dto.request.EquipoRequestDTO;
import org.bormun.aplicacion.dto.response.SolicitudResponseDTO;
import org.bormun.dominio.modelos.DatosDeportista;
import org.bormun.dominio.modelos.Deportista;
import org.bormun.dominio.modelos.Equipo;
import org.bormun.dominio.modelos.Solicitud;
import org.bormun.infraestructura.entidades.DeportistaEntidad;
import org.bormun.infraestructura.entidades.EquipoEntidad;
import org.bormun.infraestructura.entidades.SolicitudEntidad;

import java.util.ArrayList;
import java.util.List;

public class SolicitudMapper {
    public static SolicitudEntidad aEntidad(Solicitud solicitud){
        SolicitudEntidad entidad = new SolicitudEntidad();

        entidad.setId(solicitud.getId());
        entidad.setNombreOrganizacion(solicitud.getNombreOrganizacion());
        entidad.setFechaSolicitud(solicitud.getFechaSolicitud());
        entidad.setPagoConfirmado(solicitud.isPagoConfirmado());
        entidad.setPrecioTotal(solicitud.getPrecioTotal());
        entidad.setEstadoSolicitud(solicitud.getEstadoSolicitud());
        entidad.setComentarios(solicitud.getComentarios());
        entidad.setEquipo(aEntidad(solicitud.getEquipo()));

        return entidad;
    }

    public static EquipoEntidad aEntidad(Equipo equipo){
        EquipoEntidad entidad = new EquipoEntidad();
        entidad.setId(equipo.getId());
        entidad.setNombreEquipo(equipo.getNombreEquipo());

        List<DeportistaEntidad> deportistas = new ArrayList<>();
        for(Deportista deportista: equipo.getIntegrantes()){
            deportistas.add(aEntidad(deportista));
        }
        entidad.setDeportistas(deportistas);

        return entidad;
    }

    public static DeportistaEntidad aEntidad(Deportista deportista){
        DeportistaEntidad entidad = new DeportistaEntidad();
        DatosDeportista datos = deportista.getDatosDeportista();

        entidad.setIdentificacion(datos.identificacion());
        entidad.setNombre(datos.nombre());
        entidad.setGeneroNacimiento(datos.generoNacimiento());
        entidad.setFechaNacimiento(datos.fechaNacimiento());

        return entidad;
    }

    public static Solicitud aDominio(SolicitudEntidad solicitud){
        Solicitud dominio = new Solicitud(
                solicitud.getNombreOrganizacion(),
                aDominio(solicitud.getEquipo()),
                CategoriaMapper.aDominio(solicitud.getCategoria())
        );

        dominio.setId(solicitud.getId());

        dominio.setFechaSolicitud(solicitud.getFechaSolicitud());
        dominio.setPagoConfirmado(solicitud.getPagoConfirmado());
        dominio.setPrecioTotal(solicitud.getPrecioTotal());
        dominio.setEstadoSolicitud(solicitud.getEstadoSolicitud());
        dominio.setComentarios(solicitud.getComentarios());

        return dominio;
    }

    public static Equipo aDominio(EquipoEntidad equipo){
        Equipo dominio = new Equipo(equipo.getNombreEquipo());
        dominio.setId(equipo.getId());
        for (DeportistaEntidad deportista: equipo.getDeportistas()){
            dominio.agregarIntegrante(new DatosDeportista(
                    deportista.getNombre(),
                    deportista.getIdentificacion(),
                    deportista.getGeneroNacimiento(),
                    deportista.getFechaNacimiento()
            ));
        }
        return dominio;
    }

    public static Equipo aDominio(EquipoRequestDTO equipo){
        Equipo dominio = new Equipo(equipo.nombreEquipo());
        for(DeportistaRequestDTO deportista: equipo.integrantes()){
            dominio.agregarIntegrante(new DatosDeportista(
                    deportista.nombre(),
                    deportista.identificacion().toString(),
                    deportista.generoNacimiento(),
                    deportista.fechaNacimiento()
            ));
        }
        return dominio;
    }

    public static SolicitudResponseDTO aDTO(SolicitudEntidad entidad){
        return new SolicitudResponseDTO(
                entidad.getId(),
                entidad.getNombreOrganizacion(),
                entidad.getFechaSolicitud(),
                entidad.getPagoConfirmado(),
                entidad.getPrecioTotal(),
                entidad.getEstadoSolicitud(),
                CategoriaMapper.aEquipoRequest(entidad.getEquipo()),
                entidad.getCategoria().getId(),
                entidad.getCategoria().getNombreCategoria()
        );
    }
}
