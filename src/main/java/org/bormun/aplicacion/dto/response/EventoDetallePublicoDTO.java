package org.bormun.aplicacion.dto.response;

import java.util.List;

public record EventoDetallePublicoDTO(
        Long id,
        String nombre,
        boolean inscripcionAbierta,
        List<CategoriaResponseDTO> categorias
) {
}
