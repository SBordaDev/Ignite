package org.bormun.aplicacion.dto.request;

import jakarta.validation.constraints.NotNull;
import org.bormun.dominio.modelos.EstadoSolicitud;

public record ProcesarSolicitudDTO(
        @NotNull
        EstadoSolicitud estado,
        String comentarios
) {
}
