package org.bormun.usecase;

import org.bormun.domain.solicitud.EstadoSolicitud;
import org.bormun.domain.solicitud.Solicitud;

public class AceptarSolicitud {
    public void aceptarSolicitud(Solicitud solicitud){
        solicitud.setEstadoSolicitud(EstadoSolicitud.ACEPTADO);

        solicitud.getCategoria().agregarEquipo(solicitud.getEquipo());
    }
}
