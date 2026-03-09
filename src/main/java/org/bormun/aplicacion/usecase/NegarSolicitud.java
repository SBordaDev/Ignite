package org.bormun.aplicacion.usecase;

import org.bormun.dominio.modelos.EstadoSolicitud;
import org.bormun.dominio.modelos.Solicitud;

public class NegarSolicitud {
    public void negarSolicitud(Solicitud solicitud, String comenterio){
        solicitud.setEstadoSolicitud(EstadoSolicitud.RECHAZADO);
        solicitud.setComentarios(comenterio);
    }
}
