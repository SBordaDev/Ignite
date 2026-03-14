package org.bormun.dominio.repositorios;

import org.bormun.infraestructura.entidades.EventoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<EventoEntidad, Long> {
}
