package org.bormun.aplicacion.repositorios;

import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<UsuarioEntidad, Long> {

    Optional<UsuarioEntidad> findByEmail(String email);
}
