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

        // 1. Mapeamos las categorías (Aquí se crean las entidades de categorías y equipos)
        List<CategoriaEntidad> categoriasEntidad = new ArrayList<>();
        for (Categoria categoria: evento.getCategorias()){
            categoriasEntidad.add(aEntidad(categoria));
        }
        entidad.setCategorias(categoriasEntidad);

        // 2. Mapeamos las solicitudes
        List<SolicitudEntidad> solicitudesEntidad = new ArrayList<>();
        for (Solicitud solicitud: evento.getSolicitudes()){
            SolicitudEntidad solEntidad = aEntidad(solicitud);

            // 3. LA MAGIA: Conectamos la solicitud con las mismas instancias en memoria de arriba
            for (CategoriaEntidad catEntidad : categoriasEntidad) {
                if (catEntidad.getNombreCategoria().equals(solicitud.getCategoria().getNombreCategoria())) {
                    solEntidad.setCategorias(catEntidad); // ¡Misma instancia!

                    for (EquipoEntidad eqEntidad : catEntidad.getInscritos()) {
                        if (eqEntidad.getNombreEquipo().equals(solicitud.getEquipo().getNombreEquipo())) {
                            solEntidad.setEquipos(eqEntidad); // ¡Misma instancia!
                            break;
                        }
                    }
                    break;
                }
            }
            solicitudesEntidad.add(solEntidad);
        }
        entidad.setSolicitudes(solicitudesEntidad);

        return entidad;
    }

    public static CategoriaEntidad aEntidad(Categoria categoria){
        CategoriaEntidad entidad = new CategoriaEntidad();
        List<EquipoEntidad> inscritos = new ArrayList<>();

        entidad.setId(categoria.getId());
        entidad.setNombreCategoria(categoria.getNombreCategoria());
        entidad.setPrecioInscripcion(categoria.getPrecioInscripcion());
        for (Equipo equipo: categoria.getInscritos()){
            inscritos.add(aEntidad(equipo));
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

    public static SolicitudEntidad aEntidad(Solicitud solicitud){
        SolicitudEntidad entidad = new SolicitudEntidad();

        entidad.setId(solicitud.getId());
        entidad.setNombreOrganizacion(solicitud.getNombreOrganizacion());
        entidad.setFechaSolicitud(solicitud.getFechaSolicitud());
        entidad.setPagoConfirmado(solicitud.isPagoConfirmado());
        entidad.setPrecioTotal(solicitud.getPrecioTotal());
        entidad.setEstadoSolicitud(solicitud.getEstadoSolicitud());
        entidad.setComentarios(solicitud.getComentarios());
        entidad.setEquipos(aEntidad(solicitud.getEquipo()));

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

    public static Evento aDominio(EventoEntidad evento){
        Evento dominio = new Evento(evento.getNombre());
        List<Categoria> categorias = new ArrayList<>();

        dominio.setId(evento.getId());
        dominio.setInscripcionAbierta(evento.isInscripcionAbierta());
        for (CategoriaEntidad categoria: evento.getCategorias()){
            categorias.add(aDominio(categoria));
        }
        dominio.setCategorias(categorias);


        for (SolicitudEntidad solicitud: evento.getSolicitudes()){
            dominio.agregarSolicitud(aDominio(solicitud));
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

    public static Solicitud aDominio(SolicitudEntidad solicitud){
        Solicitud dominio = new Solicitud(
                solicitud.getNombreOrganizacion(),
                aDominio(solicitud.getEquipos()),
                aDominio(solicitud.getCategorias())
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

    public static Categoria aDominio(CategoriaEntidad categoria){
        Categoria dominio = new Categoria(
                categoria.getNombreCategoria(),
                categoria.getPrecioInscripcion(),
                aDominio(categoria.getRestricciones())
        );
        dominio.setId(categoria.getId());
        for (EquipoEntidad equipo: categoria.getInscritos()){
            dominio.agregarEquipo(aDominio(equipo));
        }
        return dominio;
    }
}
