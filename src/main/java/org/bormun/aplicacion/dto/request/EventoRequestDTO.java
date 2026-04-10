package org.bormun.aplicacion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record EventoRequestDTO(
        @NotBlank
        String nombre,

        @NotEmpty
        @Valid
        List<CategoriaRequestDTO> categorias
) {
}
