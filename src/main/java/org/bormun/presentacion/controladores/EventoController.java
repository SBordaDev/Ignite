package org.bormun.presentacion.controladores;

import jakarta.validation.Valid;
import org.bormun.presentacion.dto.request.EventoRequestDTO;
import org.bormun.presentacion.dto.response.EventoDetallesCreadorDTO;
import org.bormun.presentacion.dto.response.EventoResumenDTO;
import org.bormun.aplicacion.usecase.ConsultarEvento;
import org.bormun.aplicacion.usecase.CrearEvento;
import org.bormun.dominio.modelos.EstadoSolicitud;
import org.bormun.dominio.modelos.Evento;
import org.bormun.dominio.modelos.Roles;
import org.bormun.infraestructura.entidades.EventoEntidad;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.bormun.aplicacion.mapper.EventoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    private final CrearEvento crearEventoUseCase;
    private final ConsultarEvento consultarEvento;

    // Inyectamos tu Caso de Uso
    public EventoController(CrearEvento crearEventoUseCase, ConsultarEvento consultarEvento) {
        this.crearEventoUseCase = crearEventoUseCase;
        this.consultarEvento = consultarEvento;
    }

    @PostMapping
    public ResponseEntity<?> crearNuevoEvento(
            @Valid @RequestBody EventoRequestDTO dto,
            @AuthenticationPrincipal UsuarioEntidad usuarioAutenticado
            ) {

        try {
            // 1. TRADUCCIÓN: De DTO (Web) a Dominio (Núcleo)
            Evento eventoNuevo = EventoMapper.aDominio(dto);

            // 2. EJECUCIÓN: Le pasamos el dominio puro al Caso de Uso
            Evento eventoCreado = crearEventoUseCase.crearEvento(eventoNuevo, usuarioAutenticado);

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

    @GetMapping("/{id}/creador")
    public ResponseEntity<?> obtenerDetalleCreador(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioEntidad usuarioAutenticado,
            @RequestParam(required = false)EstadoSolicitud estado)
    {
        try {
            EventoEntidad entidad = consultarEvento.obtenerPorId(id);
            boolean esAdmin = usuarioAutenticado.getRol() == Roles.ADMIN;
            boolean esDuenoDelEvento = entidad.getCreador().getId().equals(usuarioAutenticado.getId());

            if (!esAdmin && !esDuenoDelEvento) {
                throw new IllegalArgumentException("Acceso Denegado: No tienes permiso para visualizar como creador un evento que no creaste.");
            }
            EventoDetallesCreadorDTO dtoFiltrado = EventoMapper.aDetalleCreadorDTO(entidad, estado);
            return ResponseEntity.ok(dtoFiltrado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}