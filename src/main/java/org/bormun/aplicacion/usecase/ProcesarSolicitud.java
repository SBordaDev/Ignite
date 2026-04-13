package org.bormun.aplicacion.usecase;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import org.bormun.presentacion.dto.request.ProcesarSolicitudDTO;
import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Equipo;
import org.bormun.dominio.modelos.EstadoSolicitud;
import org.bormun.dominio.modelos.Roles;
import org.bormun.aplicacion.repositorios.EventoRepository;
import org.bormun.aplicacion.repositorios.SolicitudRepository;
import org.bormun.infraestructura.entidades.*;
import org.bormun.aplicacion.mapper.CategoriaMapper;
import org.bormun.aplicacion.mapper.SolicitudMapper;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProcesarSolicitud {
    private final EventoRepository eventoRepository;
    private final SolicitudRepository solicitudRepository;
    private final MeterRegistry meterRegistry;

    public ProcesarSolicitud(EventoRepository eventoRepository, SolicitudRepository solicitudRepository, MeterRegistry meterRegistry) {
        this.eventoRepository = eventoRepository;
        this.solicitudRepository = solicitudRepository;
        this.meterRegistry = meterRegistry;
    }

    public void ejecutar(Long id, ProcesarSolicitudDTO data, UsuarioEntidad usuarioAutenticado){
        SolicitudEntidad solicitudEntidad = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud con id ("+id+") no fue encontrada"));


        EventoEntidad eventoDelTorneo = eventoRepository.findEventoBySolicitudId(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado para esta solicitud"));

        boolean esAdmin = usuarioAutenticado.getRol() == Roles.ADMIN;
        boolean esDuenoDelEvento = eventoDelTorneo.getCreador().getId().equals(usuarioAutenticado.getId());

        if (!esAdmin && !esDuenoDelEvento) {
            throw new IllegalArgumentException("Acceso Denegado: No tienes permiso para modificar un evento que no creaste.");
        }

        if (solicitudEntidad.getEstadoSolicitud() != EstadoSolicitud.EN_PROCESO) {
            throw new IllegalArgumentException("Esta solicitud ya fue procesada previamente y no puede ser modificada.");
        }

        solicitudEntidad.setEstadoSolicitud(data.estado());

        if (data.comentarios() != null && !data.comentarios().trim().isEmpty()) {
            solicitudEntidad.setComentarios(data.comentarios().trim());
        }

        if (data.estado() == EstadoSolicitud.ACEPTADO) {
            CategoriaEntidad categoria = solicitudEntidad.getCategoria();
            EquipoEntidad equipo = solicitudEntidad.getEquipo();

            Categoria catDominio = CategoriaMapper.aDominio(categoria);
            Equipo equipoDominio = SolicitudMapper.aDominio(equipo);

            catDominio.verificarEquipo(equipoDominio);

            categoria.getInscritos().add(equipo);
        }

        solicitudRepository.save(solicitudEntidad);

        Counter.builder("ignite.solicitudes.procesadas")
                .description("Número de solicitudes procesadas por los administradores")
                .tag("estado_final", data.estado().name())
                .register(meterRegistry)
                .increment();
    }


}
