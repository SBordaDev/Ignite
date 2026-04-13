package org.bormun.presentacion.dto.response;

import org.bormun.presentacion.dto.request.EquipoRequestDTO;
import org.bormun.presentacion.dto.request.RestriccionesRequestDTO;

import java.util.List;

public record CategoriaResponseDTO(
        Long id,
        String nombreCategoria,
        int precioInscripcion,
        List<EquipoRequestDTO> inscritos,
        RestriccionesRequestDTO restricciones
) {
}
