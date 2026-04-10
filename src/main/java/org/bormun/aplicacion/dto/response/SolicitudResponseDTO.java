package org.bormun.aplicacion.dto.response;

import org.bormun.aplicacion.dto.request.EquipoRequestDTO;
import org.bormun.dominio.modelos.EstadoSolicitud;

import java.time.LocalDate;

public record SolicitudResponseDTO(
        Long id,
        String nombreOrganizacion,
        LocalDate fechaSolicitud,
        boolean pagoConfirmado,
        int precioTotal,
        EstadoSolicitud estadoSolicitud,
        EquipoRequestDTO equipo,
        Long idCategoria,
        String nombreCategoria
) {
}
