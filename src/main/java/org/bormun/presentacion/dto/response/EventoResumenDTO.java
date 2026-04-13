package org.bormun.presentacion.dto.response;

public record EventoResumenDTO(
        Long id,
        String nombre,
        boolean inscripcionAbierta
) {
}
