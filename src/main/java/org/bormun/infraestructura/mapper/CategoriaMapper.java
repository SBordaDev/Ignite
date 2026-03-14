package org.bormun.infraestructura.mapper;

import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Equipo;
import org.bormun.dominio.modelos.Restricciones;
import org.bormun.infraestructura.entidades.CategoriaEntidad;
import org.bormun.infraestructura.entidades.EquipoEntidad;
import org.bormun.infraestructura.entidades.RestriccionesEntidad;

import java.util.ArrayList;
import java.util.List;

public class CategoriaMapper {
    public static CategoriaEntidad aEntidad(Categoria categoria){
        CategoriaEntidad entidad = new CategoriaEntidad();
        List<EquipoEntidad> inscritos = new ArrayList<>();

        entidad.setId(categoria.getId());
        entidad.setNombreCategoria(categoria.getNombreCategoria());
        entidad.setPrecioInscripcion(categoria.getPrecioInscripcion());
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
}
