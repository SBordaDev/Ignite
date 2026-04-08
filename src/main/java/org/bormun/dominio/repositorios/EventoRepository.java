package org.bormun.dominio.repositorios;

import org.bormun.infraestructura.entidades.EventoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<EventoEntidad, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    List<EventoEntidad> findByInscripcionAbiertaTrue();
}
