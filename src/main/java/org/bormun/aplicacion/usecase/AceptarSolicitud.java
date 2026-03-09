package org.bormun.aplicacion.usecase;

import org.bormun.dominio.modelos.EstadoSolicitud;
import org.bormun.dominio.modelos.Solicitud;

public class AceptarSolicitud {
    public void aceptarSolicitud(Solicitud solicitud){
        solicitud.setEstadoSolicitud(EstadoSolicitud.ACEPTADO);
        solicitud.getCategoria().agregarEquipo(solicitud.getEquipo());
    }
}
