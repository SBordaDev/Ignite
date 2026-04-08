package org.bormun.aplicacion.dto;

import java.util.List;

public record EquipoRequestDTO(
        String nombreEquipo,
        List<DeportistaRequestDTO> integrantes
) {
}
