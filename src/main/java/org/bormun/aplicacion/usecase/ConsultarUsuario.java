package org.bormun.aplicacion.usecase;

import org.bormun.aplicacion.repositorios.UsuarioRepository;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.springframework.stereotype.Service;

@Service
public class ConsultarUsuario {
    private final UsuarioRepository usuarioRepository;

    public ConsultarUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioEntidad buscarPorId(Long id){

        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el usuario con la id ("+id+")"));
    }
}
