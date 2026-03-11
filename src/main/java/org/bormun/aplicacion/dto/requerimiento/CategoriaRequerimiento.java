package org.bormun.aplicacion.dto.requerimiento;

import org.bormun.dominio.modelos.Restricciones;

import java.util.List;

public record CategoriaRequerimiento(
        String nombreCategoria,
        int precioInscripcion,
        Restricciones restricciones
) {
}
