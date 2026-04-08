package org.bormun.aplicacion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.bormun.dominio.modelos.GeneroNacimiento;

public record RestriccionesRequestDTO(
        @Min(value = 1, message = "La edad mínima debe ser minimo de 1")
        @Max(value = 80, message = "La edad maxima debe ser maximo de 80")
        @NotNull(message = "edadMinima no debe ser nula")
        int edadMinima,

        @Min(value = 1, message = "La edad mínima debee ser de 1")
        @Max(value = 80, message = "La edad maxima debe ser de 80")
        @NotNull(message = "edadMaxima no debe ser nulo")
        int edadMaxima,

        @NotNull(message = "generoNacimiento no debe ser nulo")
        GeneroNacimiento generoNacimiento,

        @NotNull(message = "numeroEquipos no debe ser nulo")
        @Min(value = 2, message = "minimo dos equipos por categoria")
        int numeroEquipo,

        @NotNull(message = "numoroIntegrantesPorEquipo no debe ser nulo")
        @Min(value = 1, message = "minimo un deportista por equipo")
        int numeroIntegrantesPorEquipo
) {
}
