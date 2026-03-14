package org.bormun.aplicacion.usecase;

import jakarta.transaction.Transactional;
import org.bormun.dominio.excepciones.ErrorDeportista;
import org.bormun.dominio.modelos.Solicitud;
import org.bormun.dominio.excepciones.SolicitudInvalidaException;
import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Evento;
import org.bormun.dominio.modelos.Deportista;
import org.bormun.dominio.modelos.Equipo;
import org.bormun.dominio.repositorios.EventoRepository;
import org.bormun.dominio.repositorios.SolicitudRepository;
import org.bormun.infraestructura.entidades.CategoriaEntidad;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.entidades.SolicitudEntidad;
import org.bormun.infraestructura.mapper.EventoMapper;
import org.bormun.infraestructura.mapper.SolicitudMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

//TODO: cuando agregues estados del evento modificar linea 43
@Service
@Transactional
public class EnviarSolicitud {
    private final EventoRepository eventoRepository;
    private final SolicitudRepository solicitudRepository;

    public EnviarSolicitud(EventoRepository eventoRepository, SolicitudRepository solicitudRepository) {
        this.eventoRepository = eventoRepository;
        this.solicitudRepository = solicitudRepository;
    }

    public void enviarSolicitud(Long idEvento, Solicitud solicitud){


        EventoEntidad eventoEntidad = eventoRepository.findById(idEvento)
                .orElseThrow(() -> new IllegalArgumentException("El ID proporcionado no fue hayado para EVENTO"));

        Evento evento = EventoMapper.aDominio(eventoEntidad);

        if(!evento.isInscripcionAbierta()){
            throw new SolicitudInvalidaException("Las inscripciones estan cerradas");
        }

        Categoria categoriaReal = evento.getCategorias().stream()
                .filter(c -> c.getNombreCategoria().equals(solicitud.getCategoria().getNombreCategoria()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La categoría no existe"));

        Equipo equipo = solicitud.getEquipo();

        List<ErrorDeportista> conErrores = new ArrayList<>();

        for (Deportista deportista : equipo.getIntegrantes()) {
            try{
                categoriaReal.verificarDeportista(deportista);
            } catch (ErrorDeportista e) {
                conErrores.add(e);
            }
        }

        if(!conErrores.isEmpty()){
            throw new SolicitudInvalidaException("Deportistas no cumplen con los requisitos", conErrores);
        }

        categoriaReal.verificarEquipo(equipo);

        Solicitud solicitudValida = new Solicitud(
                solicitud.getNombreOrganizacion(),
                equipo,
                categoriaReal
        );


        solicitudValida.actualizarPrecioTotal(categoriaReal.getPrecioInscripcion());

        SolicitudEntidad solicitudEntidad = SolicitudMapper.aEntidad(solicitudValida);
        CategoriaEntidad catEntidadDestino = eventoEntidad.getCategorias().stream()
                .filter(c -> c.getNombreCategoria().equals(categoriaReal.getNombreCategoria()))
                .findFirst().get();

        solicitudEntidad.setCategoria(catEntidadDestino);
        solicitudRepository.save(solicitudEntidad);
    }
}
