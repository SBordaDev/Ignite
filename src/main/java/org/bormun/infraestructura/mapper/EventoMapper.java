package org.bormun.infraestructura.mapper;

import org.bormun.aplicacion.dto.request.CategoriaRequestDTO;
import org.bormun.aplicacion.dto.request.EventoRequestDTO;
import org.bormun.aplicacion.dto.request.RestriccionesRequestDTO;
import org.bormun.aplicacion.dto.response.*;
import org.bormun.dominio.modelos.*;
import org.bormun.infraestructura.entidades.*;

import java.util.ArrayList;
import java.util.List;

public class EventoMapper {

    public static EventoEntidad aEntidad(Evento evento){
        EventoEntidad entidad = new EventoEntidad();
        entidad.setId(evento.getId());
        entidad.setNombre(evento.getNombre());
        entidad.setInscripcionAbierta(evento.isInscripcionAbierta());

        List<CategoriaEntidad> categoriasEntidad = new ArrayList<>();
        for (Categoria categoria: evento.getCategorias()){
            categoriasEntidad.add(CategoriaMapper.aEntidad(categoria, evento.getSolicitudes()));
        }
        entidad.setCategorias(categoriasEntidad);

        return entidad;
    }

    public static Evento aDominio(EventoEntidad evento){
        Evento dominio = new Evento(evento.getNombre());

        dominio.setId(evento.getId());
        dominio.setInscripcionAbierta(evento.isInscripcionAbierta());
        for (CategoriaEntidad categoria: evento.getCategorias()){
            RestriccionesEntidad restriccionesEntidad = categoria.getRestricciones();
            dominio.agregarCategoria(
                    categoria.getNombreCategoria(),
                    categoria.getPrecioInscripcion(),
                    new Restricciones(
                            restriccionesEntidad.getEdadMinima(),
                            restriccionesEntidad.getEdadMaxima(),
                            restriccionesEntidad.getGeneroNacimiento(),
                            restriccionesEntidad.getNumeroEquipo(),
                            restriccionesEntidad.getNumeroIntegrantesPorEquipo()
                    ));

            for (SolicitudEntidad solicitudEntidad: categoria.getSolicitudes()){
                dominio.agregarSolicitud(SolicitudMapper.aDominio(solicitudEntidad));
            }
        }

        return dominio;
    }

    public static Evento aDominio(EventoRequestDTO dto){
        Evento eventoNuevo = new Evento(dto.nombre());

        for (CategoriaRequestDTO catDTO : dto.categorias()) {

            RestriccionesRequestDTO restDTO = catDTO.restricciones();

            Restricciones restricciones = new Restricciones(
                    restDTO.edadMinima(),
                    restDTO.edadMaxima(),
                    restDTO.generoNacimiento(),
                    restDTO.numeroEquipo(),
                    restDTO.numeroIntegrantesPorEquipo()
            );

            eventoNuevo.agregarCategoria(
                    catDTO.nombreCategoria(),
                    catDTO.precioInscripcion(),
                    restricciones
            );
        }
        return eventoNuevo;
    }

    public static EventoResumenDTO aResumenDTO(EventoEntidad entidad){
        return new EventoResumenDTO(
                entidad.getId(),
                entidad.getNombre(),
                entidad.isInscripcionAbierta()
        );
    }

    public static EventoDetallePublicoDTO aDetallePublicoDTO(EventoEntidad entidad){
        List<CategoriaResponseDTO> categorias = new ArrayList<>();
        for(CategoriaEntidad categoria: entidad.getCategorias()){
            categorias.add(CategoriaMapper.aCategoriaResponseDTO(categoria));
        }

        return new EventoDetallePublicoDTO(
                entidad.getId(),
                entidad.getNombre(),
                entidad.isInscripcionAbierta(),
                categorias
        );
    }

    public static EventoDetallesCreadorDTO aDetalleCreadorDTO(EventoEntidad entidad){
        List<CategoriaResponseDTO> categorias = new ArrayList<>();
        List<SolicitudResponseDTO> solicitudes = new ArrayList<>();

        for(CategoriaEntidad categoria: entidad.getCategorias()){
            categorias.add(CategoriaMapper.aCategoriaResponseDTO(categoria));
            for(SolicitudEntidad solicitud: categoria.getSolicitudes()){
                solicitudes.add((SolicitudMapper.aDTO(solicitud)));
            }
        }

        return new EventoDetallesCreadorDTO(
                entidad.getId(),
                entidad.getNombre(),
                entidad.isInscripcionAbierta(),
                categorias,
                solicitudes
        );
    }
}
