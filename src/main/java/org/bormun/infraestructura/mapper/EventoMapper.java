package org.bormun.infraestructura.mapper;

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
            categoriasEntidad.add(CategoriaMapper.aEntidad(categoria));
        }
        entidad.setCategorias(categoriasEntidad);

        return entidad;
    }

    public static Evento aDominio(EventoEntidad evento){
        Evento dominio = new Evento(evento.getNombre());
        List<Categoria> categorias = new ArrayList<>();

        dominio.setId(evento.getId());
        dominio.setInscripcionAbierta(evento.isInscripcionAbierta());
        for (CategoriaEntidad categoria: evento.getCategorias()){
            categorias.add(CategoriaMapper.aDominio(categoria));
        }

        dominio.setCategorias(categorias);

        return dominio;
    }
}
