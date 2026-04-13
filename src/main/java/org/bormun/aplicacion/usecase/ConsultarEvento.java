package org.bormun.aplicacion.usecase;

import org.bormun.aplicacion.repositorios.EventoRepository;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConsultarEvento {
    private final EventoRepository eventoRepository;

    public ConsultarEvento(EventoRepository eventoRepository){
        this.eventoRepository = eventoRepository;
    }

    public EventoEntidad obtenerPorId(Long id){
        return eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El evento con el id ("+id+") no fue encontrado"));
    }

    public List<EventoEntidad> listarAbiertos(){
        return eventoRepository.findByInscripcionAbiertaTrue();
    }
}
