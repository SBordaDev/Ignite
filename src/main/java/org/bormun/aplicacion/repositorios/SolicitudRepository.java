package org.bormun.aplicacion.repositorios;

import org.bormun.infraestructura.entidades.SolicitudEntidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitudRepository extends JpaRepository<SolicitudEntidad, Long> {

}
