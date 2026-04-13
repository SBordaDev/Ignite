package org.bormun.presentacion.controladores;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.bormun.presentacion.dto.request.ProcesarSolicitudDTO;
import org.bormun.presentacion.dto.request.SolicitudRequestDTO;
import org.bormun.aplicacion.usecase.ProcesarSolicitud;
import org.bormun.aplicacion.usecase.EnviarSolicitud;
import org.bormun.infraestructura.entidades.UsuarioEntidad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class SolicitudController {

    private final EnviarSolicitud enviarSolicitudUseCase;
    private final ProcesarSolicitud procesarSolicitudUseCase;

    public SolicitudController(EnviarSolicitud enviarSolicitudUseCase, ProcesarSolicitud procesarSolicitudUseCase) {
        this.enviarSolicitudUseCase = enviarSolicitudUseCase;
        this.procesarSolicitudUseCase = procesarSolicitudUseCase;
    }

    // El POST es a nivel de evento, la categoría va por dentro del JSON
    @PostMapping("/eventos/{eventoId}/solicitudes")
    @RateLimiter(name = "envioSolicitudes")
    public ResponseEntity<?> enviar(
            @PathVariable Long eventoId,
            @Valid @RequestBody SolicitudRequestDTO dto) {

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