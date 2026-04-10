package org.bormun.aplicacion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoriaRequestDTO(
        @NotBlank
        String nombreCategoria,

        @Min(value = 0, message = "el precio de incripcion no debe ser negativo")
        int precioInscripcion,

        @NotNull
        @Valid
        RestriccionesRequestDTO restricciones
) {
}
