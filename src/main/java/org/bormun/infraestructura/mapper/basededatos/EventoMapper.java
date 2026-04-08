package org.bormun.infraestructura.mapper.basededatos;

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
}
