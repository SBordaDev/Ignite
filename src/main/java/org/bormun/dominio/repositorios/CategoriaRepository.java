package org.bormun.dominio.repositorios;

import org.bormun.infraestructura.entidades.CategoriaEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<CategoriaEntidad, Long> {
}
