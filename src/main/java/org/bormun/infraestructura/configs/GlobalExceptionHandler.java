package org.bormun.infraestructura.configs;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.bormun.dominio.excepciones.ErrorDeportista;
import org.bormun.dominio.excepciones.SolicitudInvalidaException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
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

    // 3. Atrapa TUS excepciones personalizadas del Dominio
    @ExceptionHandler({ErrorDeportista.class, SolicitudInvalidaException.class})
    public ResponseEntity<Map<String, String>> manejarErroresDelDominio(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Violación de reglas del torneo", "detalle", ex.getMessage()));
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
}
