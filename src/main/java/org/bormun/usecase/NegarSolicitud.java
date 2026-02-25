package org.bormun.usecase;

import org.bormun.domain.solicitud.EstadoSolicitud;
import org.bormun.domain.solicitud.Solicitud;

public class NegarSolicitud {
    public void negarSolicitud(Solicitud solicitud, String comenterio){
        solicitud.setEstadoSolicitud(EstadoSolicitud.RECHAZADO);
        solicitud.setComentarios(comenterio);
    }
}
