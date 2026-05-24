package org.bormun.presentacion;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.bormun.dominio.excepciones.ErrorDeportista;
import org.bormun.dominio.excepciones.SolicitudInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Atrapa los errores de validación de los DTOs (Los @NotNull, @NotBlank, @Min, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarErroresDeValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        // Extraemos cada campo que falló y su mensaje específico
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("error", "Error en los datos enviados");
        respuesta.put("detalles", errores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // 2. Atrapa los errores de lógica de negocio genéricos (Tus IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> manejarErroresDeArgumento(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // 4. El "Atrapa-Todo" para errores reales del servidor (Bases de datos caídas, NullPointers)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> manejarErroresInesperados(Exception ex) {
        // En un proyecto real, aquí guardarías 'ex.getMessage()' en un archivo de logs
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Ocurrió un error interno en el servidor. Por favor, contacte a soporte."));
    }

    // 5. atrapa los errores generados por exceso de solicitudes enviadas
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<Map<String, String>> manejarRateLimit(RequestNotPermitted ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(Map.of(
                        "error", "Demasiadas peticiones.",
                        "detalle", "Por favor, espera un minuto antes de enviar una nueva solicitud de inscripción."
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> manejarErrorDeLecturaJSON(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Formato de datos inválido. Verifique que no esté enviando texto vacío en campos numéricos o listas."));
    }

    // 1. Atrapa los errores cuando se rechaza una solicitud completa debido a uno o varios deportistas
    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<Map<String, Object>> manejarSolicitudInvalida(SolicitudInvalidaException ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("error", ex.getMessage());

        if (ex.getCulpables() != null && !ex.getCulpables().isEmpty()) {
            List<Map<String, Object>> listaDetalles = new ArrayList<>();

            for (ErrorDeportista errorDep : ex.getCulpables()) {
                Map<String, Object> infoCulpable = new HashMap<>();
                infoCulpable.put("motivoError", errorDep.getMotivoError().name());

                // Rescatamos el objeto de dominio completo para no perder datos
                if (errorDep.getDeportista() != null) {
                    infoCulpable.put("deportista", errorDep.getDeportista());
                }
                listaDetalles.add(infoCulpable);
            }
            respuesta.put("detalles", listaDetalles);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    // 2. Atrapa los errores cuando un solo deportista dispara directamente la anomalía
    @ExceptionHandler(ErrorDeportista.class)
    public ResponseEntity<Map<String, Object>> manejarErrorDeportista(ErrorDeportista ex) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("error", "Regla de negocio deportiva violada");

        Map<String, Object> detalles = new HashMap<>();
        detalles.put("motivoError", ex.getMotivoError().name());

        // Rescatamos el objeto de dominio completo
        if (ex.getDeportista() != null) {
            detalles.put("deportista", ex.getDeportista());
        }
        respuesta.put("detalles", detalles);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }
}
