package org.bormun.aplicacion.usecase;

import org.bormun.dominio.modelos.Evento;
import org.bormun.dominio.repositorios.EventoRepository;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.mapper.EventoMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultarEvento {
    private final EventoRepository eventoRepository;

    public ConsultarEvento(EventoRepository eventoRepository){
        this.eventoRepository = eventoRepository;
    }

    public Evento obtenerPorId(Long id){
        return EventoMapper.aDominio(
                eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("El evento con id ("+id+") no fue encontrado")));
    }

    public List<Evento> listarAbiertos(){
        List<Evento> respuesta = new ArrayList<>();
        for (EventoEntidad x: eventoRepository.findByInscripcionAbiertaTrue()){
            respuesta.add(EventoMapper.aDominio(x));
        }

        return  respuesta;
    }
}
