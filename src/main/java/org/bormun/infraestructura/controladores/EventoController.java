package org.bormun.infraestructura.controladores;

import jakarta.validation.Valid;
import org.bormun.aplicacion.dto.request.EventoRequestDTO;
import org.bormun.aplicacion.dto.response.EventoDetallesCreadorDTO;
import org.bormun.aplicacion.dto.response.EventoResumenDTO;
import org.bormun.aplicacion.usecase.ConsultarEvento;
import org.bormun.aplicacion.usecase.CrearEvento;
import org.bormun.dominio.modelos.EstadoSolicitud;
import org.bormun.dominio.modelos.Evento;
import org.bormun.dominio.repositorios.EventoRepository;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.mapper.EventoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final CrearEvento crearEventoUseCase;
    private final ConsultarEvento consultarEvento;
    private final EventoRepository eventoRepository;

    // Inyectamos tu Caso de Uso
    public EventoController(CrearEvento crearEventoUseCase, ConsultarEvento consultarEvento, EventoRepository eventoRepository) {
        this.crearEventoUseCase = crearEventoUseCase;
        this.consultarEvento = consultarEvento;
        this.eventoRepository = eventoRepository;
    }

    @PostMapping
    public ResponseEntity<?> crearNuevoEvento(@Valid @RequestBody EventoRequestDTO dto) {

        try {
            // 1. TRADUCCIÓN: De DTO (Web) a Dominio (Núcleo)
            Evento eventoNuevo = EventoMapper.aDominio(dto);

            // 2. EJECUCIÓN: Le pasamos el dominio puro al Caso de Uso
            Evento eventoCreado = crearEventoUseCase.crearEvento(eventoNuevo);

            // 3. RESPUESTA: Devolvemos un 201 Created y el ID generado
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Evento creado exitosamente");
            respuesta.put("eventoId", eventoCreado.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);

        } catch (IllegalArgumentException e) {
            // Si alguna validación de negocio de tu Dominio falla, devolvemos 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Si falla la base de datos, imprimimos el error y devolvemos 500
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error en el servidor al guardar el evento"));
        }
    }

    @GetMapping("/abiertos")
    public ResponseEntity<List<EventoResumenDTO>> listarAbiertos() {
        List<EventoResumenDTO> resumenes = consultarEvento.listarAbiertos().stream()
                .map(EventoMapper::aResumenDTO)
                .toList();
        return ResponseEntity.ok(resumenes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerDetallePublico(@PathVariable Long id){
        try{
            EventoEntidad entidad = consultarEvento.obtenerPorId(id);
            return ResponseEntity.ok(EventoMapper.aDetallePublicoDTO(entidad));

        }catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/admin")
    public ResponseEntity<?> obtenerDetalleCreador(
            @PathVariable Long id,
            @RequestParam(required = false)EstadoSolicitud estado)
    {
        try {
            EventoEntidad entidad = consultarEvento.obtenerPorId(id);
            EventoDetallesCreadorDTO dtoFiltrado = EventoMapper.aDetalleCreadorDTO(entidad, estado);
            return ResponseEntity.ok(dtoFiltrado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}