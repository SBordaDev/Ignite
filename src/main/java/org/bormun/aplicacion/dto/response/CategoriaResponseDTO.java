package org.bormun.aplicacion.dto.response;

import org.bormun.aplicacion.dto.request.EquipoRequestDTO;
import org.bormun.aplicacion.dto.request.RestriccionesRequestDTO;

import java.util.List;

public record CategoriaResponseDTO(
        Long id,
        String nombreCategoria,
        int precioInscripcion,
        List<EquipoRequestDTO> inscritos,
        RestriccionesRequestDTO restricciones
) {
}
