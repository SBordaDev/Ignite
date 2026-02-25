package org.bormun.usecase;

import org.bormun.domain.solicitud.Solicitud;
import org.bormun.domain.solicitud.SolicitudInvalidaException;
import org.bormun.domain.categoria.Categoria;
import org.bormun.domain.evento.Evento;
import org.bormun.domain.participante.Deportista;
import org.bormun.domain.participante.Equipo;

import java.util.ArrayList;
import java.util.List;

public class EnviarSolicitud {
    public void enviarSolicitud(Evento evento, Solicitud solicitud){
        Equipo equipo = solicitud.getEquipo();
        List<Deportista> deportistas = equipo.getIntegrantes();
        Categoria categoria = solicitud.getCategoria();

        List<Deportista> conErrores = new ArrayList<>();

        for (Deportista deportista : deportistas) {
            categoria.verificarDeportista(deportista);

        }

        if(!conErrores.isEmpty()){
            throw new SolicitudInvalidaException("Hay deportistas que no cumplen con los requisitos", conErrores);
        }

        categoria.verificarEquipo(equipo);
        solicitud.setPrecioTotal(categoria.getPrecioInscripcion());
        evento.agregarSolicitud(solicitud);
    }
}
