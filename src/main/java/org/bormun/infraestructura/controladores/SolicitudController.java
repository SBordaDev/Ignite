package org.bormun.infraestructura.controladores;

import jakarta.validation.Valid;
import org.bormun.aplicacion.dto.request.ProcesarSolicitudDTO;
import org.bormun.aplicacion.dto.request.SolicitudRequestDTO;
import org.bormun.aplicacion.usecase.ProcesarSolicitud;
import org.bormun.aplicacion.usecase.EnviarSolicitud;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @Valid @RequestBody ProcesarSolicitudDTO dto) {

        procesarSolicitudUseCase.ejecutar(solicitudId, dto);
        String mensajeRetorno = "";
        if(dto.estado().name().equals("ACEPTADA")){
            mensajeRetorno = "Solicitud aceptada exitosamente";
        } else if (dto.estado().name().equals("RECHAZADO")) {
            mensajeRetorno = "Solicitud rechazada exitosamente.";
        }else{
            mensajeRetorno = "algo paso";
        }

        return ResponseEntity.ok(Map.of("mensaje", mensajeRetorno));
    }
}