package org.bormun.aplicacion.usecase;

import org.bormun.dominio.excepciones.ErrorDeportista;
import org.bormun.dominio.modelos.Solicitud;
import org.bormun.dominio.excepciones.SolicitudInvalidaException;
import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Evento;
import org.bormun.dominio.modelos.Deportista;
import org.bormun.dominio.modelos.Equipo;

import java.util.ArrayList;
import java.util.List;

public class EnviarSolicitud {
    public void enviarSolicitud(Evento evento, Solicitud solicitud){
        if(!evento.isInscripcionAbierta()){
            throw new SolicitudInvalidaException("Las inscripciones estan cerradas");
        }

        Equipo equipo = solicitud.getEquipo();
        List<Deportista> deportistas = equipo.getIntegrantes();
        Categoria categoria = solicitud.getCategoria();

        List<ErrorDeportista> conErrores = new ArrayList<>();

        for (Deportista deportista : deportistas) {
            try{
                categoria.verificarDeportista(deportista);
            } catch (ErrorDeportista e) {
                conErrores.add(e);
            }
        }

        if(!conErrores.isEmpty()){
            throw new SolicitudInvalidaException("Hay deportistas que no cumplen con los requisitos", conErrores);
        }

        categoria.verificarEquipo(equipo);
        solicitud.setPrecioTotal(categoria.getPrecioInscripcion());
        evento.agregarSolicitud(solicitud);
    }
}
