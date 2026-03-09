package org.bormun.dominio.modelos;

//TODO: agregar restricciones de peso y altura
//TODO: agregar restriccion minIntegrantes
public record Restricciones(int edadMinima, int edadMaxima, GeneroNacimiento generoNacimiento, int numeroEquipo,
                            int numeroIntegrantesPorEquipo) {
}
