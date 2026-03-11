package org.bormun.aplicacion.dto.requerimiento;

import org.bormun.dominio.modelos.Categoria;

import java.util.List;

public record EventoRequerimiento(
        String nombre,
        List<CategoriaRequerimiento> categorias
) {
}
