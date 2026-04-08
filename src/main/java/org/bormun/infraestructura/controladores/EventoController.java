package org.bormun.infraestructura.controladores;

import jakarta.validation.Valid;
import org.bormun.aplicacion.dto.CategoriaRequestDTO;
import org.bormun.aplicacion.dto.EventoRequestDTO;
import org.bormun.aplicacion.dto.RestriccionesRequestDTO;
import org.bormun.aplicacion.usecase.ConsultarEvento;
import org.bormun.aplicacion.usecase.CrearEvento;
import org.bormun.dominio.modelos.Evento;
import org.bormun.dominio.modelos.Restricciones;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
    public ResponseEntity<?> crearNuevoEvento(@Valid @RequestBody EventoRequestDTO dto) {

        try {
            // 1. TRADUCCIÓN: De DTO (Web) a Dominio (Núcleo)
            Evento eventoNuevo = getEventoNuevo(dto);

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

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerEvento(@PathVariable Long id){
        consultarEvento.obtenerPorId(id);
    }

    @NonNull
    private static Evento getEventoNuevo(EventoRequestDTO dto) {
        Evento eventoNuevo = new Evento(dto.nombre());

        for (CategoriaRequestDTO catDTO : dto.categorias()) {

            RestriccionesRequestDTO restDTO = catDTO.restricciones();

            // Construimos las restricciones de dominio
            Restricciones restricciones = new Restricciones(
                    restDTO.edadMinima(),
                    restDTO.edadMaxima(),
                    restDTO.generoNacimiento(),
                    restDTO.numeroEquipo(),
                    restDTO.numeroIntegrantesPorEquipo()
            );

            // Usamos el método que ya tienes en tu dominio para agregar categorías
            eventoNuevo.agregarCategoria(
                    catDTO.nombreCategoria(),
                    catDTO.precioInscripcion(),
                    restricciones
            );
        }
        return eventoNuevo;
    }
}