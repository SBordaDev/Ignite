package org.bormun.aplicacion.repositorios;

import org.bormun.infraestructura.entidades.CategoriaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<CategoriaEntidad, Long> {
}
