package org.bormun.aplicacion.dto.response;

import org.bormun.aplicacion.dto.request.SolicitudRequestDTO;

import java.util.List;

public record EventoDetallesCreadorDTO(
        Long id,
        String nombre,
        boolean inscripcionAbierta,
        List<CategoriaResponseDTO> categorias,
        List<SolicitudResponseDTO> solicitudes
) {
}
