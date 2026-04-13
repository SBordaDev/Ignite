package org.bormun.aplicacion.usecase;

import jakarta.transaction.Transactional;
import org.bormun.presentacion.dto.request.SolicitudRequestDTO;
import org.bormun.dominio.modelos.Solicitud;
import org.bormun.dominio.modelos.Categoria;
import org.bormun.dominio.modelos.Equipo;
import org.bormun.aplicacion.repositorios.EventoRepository;
import org.bormun.aplicacion.repositorios.SolicitudRepository;
import org.bormun.infraestructura.entidades.CategoriaEntidad;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.entidades.SolicitudEntidad;
import org.bormun.aplicacion.mapper.CategoriaMapper;
import org.bormun.aplicacion.mapper.SolicitudMapper;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EnviarSolicitud {
    private final EventoRepository eventoRepository;
    private final SolicitudRepository solicitudRepository;

    public EnviarSolicitud(EventoRepository eventoRepository, SolicitudRepository solicitudRepository) {
        this.eventoRepository = eventoRepository;
        this.solicitudRepository = solicitudRepository;
    }

    @Transactional
    public void ejecutar(Long eventoId, SolicitudRequestDTO dto) {

        // 1. Obtener el evento
        EventoEntidad eventoEntidad = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (!eventoEntidad.isInscripcionAbierta()) {
            throw new IllegalArgumentException("Las inscripciones para este evento están cerradas.");
        }

        // 2. Extraer la categoría específica usando el ID que viene en el DTO
        CategoriaEntidad catEntidad = eventoEntidad.getCategorias().stream()
                .filter(c -> c.getId().equals((long) dto.idCategoria()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La categoría no pertenece a este evento"));

        // 3. Traducción DTO -> Dominio para la validación
        Categoria categoriaDominio = CategoriaMapper.aDominio(catEntidad);
        Equipo equipoDominio = SolicitudMapper.aDominio(dto.equipo()); // ¡Transformamos el equipo!

        // Construimos la solicitud
        Solicitud nuevaSolicitud = new Solicitud(
                dto.nombreOrganizacion(),
                equipoDominio,
                categoriaDominio
        );

        // 4. Validaciones de Negocio (Edades, Género, Cupos)
        for (var deportista : equipoDominio.getIntegrantes()) {
            categoriaDominio.verificarDeportista(deportista);
        }
        categoriaDominio.verificarEquipo(equipoDominio);

        nuevaSolicitud.actualizarPrecioTotal(categoriaDominio.getPrecioInscripcion());

        // 5. Traducción Dominio -> Entidad (Evitando clones)
        SolicitudEntidad solicitudEntidad = SolicitudMapper.aEntidad(nuevaSolicitud);

        solicitudEntidad.setCategoria(catEntidad);

        // 6. Guardar
        solicitudRepository.save(solicitudEntidad);
    }
}
