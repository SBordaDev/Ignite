package org.bormun.dominio.modelos;

//TODO: agregar restricciones de peso y altura
//TODO: agregar restriccion minIntegrantes
public record Restricciones(
        int edadMinima,
        int edadMaxima,
        GeneroNacimiento generoNacimiento,
        int numeroEquipo,
        int numeroIntegrantesPorEquipo
) {
    public Restricciones {
        if (edadMaxima < edadMinima) {
            throw new IllegalArgumentException("La edad máxima (" + edadMaxima + ") no puede ser menor que la edad mínima (" + edadMinima + ").");
        }

        if (edadMinima <= 0) {
            throw new IllegalArgumentException("Las edades deben ser mayores a cero.");
        }
    }
}
