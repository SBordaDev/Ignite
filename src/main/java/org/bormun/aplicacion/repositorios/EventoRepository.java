package org.bormun.aplicacion.repositorios;

import org.bormun.infraestructura.entidades.EventoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventoRepository extends JpaRepository<EventoEntidad, Long> {
    boolean existsByNombreIgnoreCase(String nombre);

    List<EventoEntidad> findByInscripcionAbiertaTrue();

    @Query("SELECT e FROM EventoEntidad e JOIN e.categorias c JOIN c.solicitudes s WHERE s.id = :solicitudId")
    Optional<EventoEntidad> findEventoBySolicitudId(@Param("solicitudId") Long solicitudId);
}
