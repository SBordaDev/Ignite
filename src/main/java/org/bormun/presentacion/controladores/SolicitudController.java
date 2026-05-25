package org.bormun.presentacion.controladores;

import jakarta.validation.Valid;
import org.bormun.presentacion.dto.request.ProcesarSolicitudDTO;
import org.bormun.presentacion.dto.request.SolicitudRequestDTO;
import org.bormun.aplicacion.usecase.ProcesarSolicitud;
import org.bormun.aplicacion.usecase.EnviarSolicitud;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import io.github.resilience4j.ratelimiter.RateLimiter; // ¡Esta es la correcta!
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SolicitudController {

    private final EnviarSolicitud enviarSolicitudUseCase;
    private final ProcesarSolicitud procesarSolicitudUseCase;
    private final RateLimiterRegistry rateLimiterRegistry;

    public SolicitudController(EnviarSolicitud enviarSolicitudUseCase, ProcesarSolicitud procesarSolicitudUseCase, RateLimiterRegistry rateLimiterRegistry) {
        this.enviarSolicitudUseCase = enviarSolicitudUseCase;
        this.procesarSolicitudUseCase = procesarSolicitudUseCase;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    // El POST es a nivel de evento, la categoría va por dentro del JSON
    @PostMapping("/eventos/{eventoId}/solicitudes")
    public ResponseEntity<?> enviar(
            @PathVariable Long eventoId,
            @Valid @RequestBody SolicitudRequestDTO dto) {

        String usuarioActual = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Creamos o recuperamos un limitador EXCLUSIVO para este usuario usando la plantilla
        String nombreLimitador = "solicitudes_" + usuarioActual;
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(nombreLimitador, "plantillaSolicitudes");

        // 3. Solicitamos permiso a la cubeta de este usuario específico
        if (!rateLimiter.acquirePermission()) {
            // Si ya gastó sus 5 peticiones, disparamos la excepción manualmente
            throw RequestNotPermitted.createRequestNotPermitted(rateLimiter);
        }

        enviarSolicitudUseCase.ejecutar(eventoId, dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Solicitud enviada correctamente y en proceso de revisión."));
    }

    @PatchMapping("/solicitudes/{solicitudId}/procesar")
    public ResponseEntity<?> procesar(
            @PathVariable Long solicitudId,
            @AuthenticationPrincipal UsuarioEntidad usuarioAutenticado,
            @Valid @RequestBody ProcesarSolicitudDTO dto) {

        procesarSolicitudUseCase.ejecutar(solicitudId, dto, usuarioAutenticado);
        String mensajeRetorno = "";
        if(dto.estado().name().equals("ACEPTADO")){
            mensajeRetorno = "Solicitud aceptada exitosamente";
        } else if (dto.estado().name().equals("RECHAZADO")) {
            mensajeRetorno = "Solicitud rechazada exitosamente.";
        }else{
            mensajeRetorno = "No has procesado la solicitud, sigue EN_PROCESO";
        }

        return ResponseEntity.ok(Map.of("mensaje", mensajeRetorno));
    }
}