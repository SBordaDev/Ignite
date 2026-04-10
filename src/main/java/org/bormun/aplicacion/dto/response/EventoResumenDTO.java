package org.bormun.aplicacion.dto.response;

public record EventoResumenDTO(
        Long id,
        String nombre,
        boolean inscripcionAbierta
) {
}
