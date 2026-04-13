package org.bormun.presentacion.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.bormun.dominio.modelos.GeneroNacimiento;

import java.time.LocalDate;

public record DeportistaRequestDTO(
        @NotBlank
        @NotNull
        String nombre,

        @NotNull
        Long identificacion,

        @NotNull
        GeneroNacimiento generoNacimiento,

        @NotNull
        LocalDate fechaNacimiento
) {
}
