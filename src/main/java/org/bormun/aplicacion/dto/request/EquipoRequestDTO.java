package org.bormun.aplicacion.dto.request;

import java.util.List;

public record EquipoRequestDTO(
        String nombreEquipo,
        List<DeportistaRequestDTO> integrantes
) {
}
