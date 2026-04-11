package org.bormun.aplicacion.usecase;

import jakarta.transaction.Transactional;
import org.bormun.aplicacion.dto.request.ProcesarSolicitudDTO;
import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Equipo;
import org.bormun.dominio.modelos.EstadoSolicitud;
import org.bormun.dominio.repositorios.SolicitudRepository;
import org.bormun.infraestructura.entidades.CategoriaEntidad;
import org.bormun.infraestructura.entidades.EquipoEntidad;
import org.bormun.infraestructura.entidades.SolicitudEntidad;
import org.bormun.infraestructura.mapper.CategoriaMapper;
import org.bormun.infraestructura.mapper.SolicitudMapper;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ProcesarSolicitud {
    private final SolicitudRepository solicitudRepository;

    public ProcesarSolicitud(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    public void ejecutar(Long id, ProcesarSolicitudDTO data){
        SolicitudEntidad solicitudEntidad = solicitudRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La solicitud con id ("+id+") no fue encontrada"));

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
    }


}
