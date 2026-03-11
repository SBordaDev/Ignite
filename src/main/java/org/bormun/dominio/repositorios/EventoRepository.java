package org.bormun.dominio.repositorios;

import org.bormun.dominio.modelos.Evento;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventoRepository extends JpaRepository<EventoEntidad, Long> {

}
