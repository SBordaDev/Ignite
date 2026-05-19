package org.bormun.evento;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.bormun.dominio.excepciones.ErrorDeportista;
import org.bormun.dominio.excepciones.SolicitudInvalidaException;
import org.bormun.dominio.modelos.MotivoErrorDeportista;
import org.bormun.presentacion.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Inicializamos MockMvc inyectando el controlador de pruebas y el manejador global
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // 1. Test para MethodArgumentNotValidException (@Valid)
    @Test
    void manejarErroresDeValidacion_DeberiaRetornar400YDetalles() throws Exception {
        mockMvc.perform(get("/test/validacion")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error en los datos enviados"))
                .andExpect(jsonPath("$.detalles.campoPrueba").value("El campo no puede estar vacío"));
    }

    // 2. Test para IllegalArgumentException
    @Test
    void manejarErroresDeLogica_DeberiaRetornar400YMensaje() throws Exception {
        mockMvc.perform(get("/test/ilegal")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Argumento inválido o recurso no encontrado"));
    }

    // 3. Test para Excepciones del Dominio (ErrorDeportista / SolicitudInvalidaException)
    @Test
    void manejarErroresDelDominio_DeberiaRetornar400YDetalleTorneo() throws Exception {
        mockMvc.perform(get("/test/dominio")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Violación de reglas del torneo"))
                .andExpect(jsonPath("$.detalle").value("El deportista ya está inscrito en otra categoría"));
    }

    // 4. Test para Rate Limiting (Resilience4j)
    @Test
    void manejarRateLimit_DeberiaRetornar429YDetallePeticiones() throws Exception {
        mockMvc.perform(get("/test/ratelimit")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Demasiadas peticiones."))
                .andExpect(jsonPath("$.detalle").value("Por favor, espera un minuto antes de enviar una nueva solicitud de inscripción."));    }

    // 5. Test para el "Atrapa-Todo" (Exception.class - Errores Inesperados 500)
    @Test
    void manejarErroresInesperados_DeberiaRetornar500YMensajeSoporte() throws Exception {
        mockMvc.perform(get("/test/inesperado")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Ocurrió un error interno en el servidor. Por favor, contacte a soporte."));
    }

    /* Controlador Falso de apoyo (Inner Class) exclusivo para simular los fallos
     */
    @RestController
    static class TestController {

        @GetMapping("/test/validacion")
        public void lanzarValidacion() throws MethodArgumentNotValidException {
            // Construimos manualmente el fallo de validación para el DTO
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objetoPrueba");
            bindingResult.addError(new FieldError("objetoPrueba", "campoPrueba", "El campo no puede estar vacío"));

            throw new MethodArgumentNotValidException(
                    Mockito.mock(MethodParameter.class),
                    bindingResult
            );
        }

        @GetMapping("/test/ilegal")
        public void lanzarIlegal() {
            throw new IllegalArgumentException("Argumento inválido o recurso no encontrado");
        }

        @GetMapping("/test/dominio")
        public void lanzarDominio() {
            // Usamos una de tus excepciones personalizadas del dominio
            throw new SolicitudInvalidaException("El deportista ya está inscrito en otra categoría");
        }

        @GetMapping("/test/ratelimit")
        public void lanzarRateLimit() {
            // Simulamos el disparo del RateLimiter de Resilience4j
            RequestNotPermitted mockException = Mockito.mock(RequestNotPermitted.class);
            Mockito.lenient().when(mockException.getMessage()).thenReturn("Rate limit exceeded");
            throw mockException;
        }

        @GetMapping("/test/inesperado")
        public void lanzarInesperado() {
            // Provocamos un error genérico imprevisto (como una caída de base de datos)
            throw new NullPointerException("Simulación de error de puntero nulo en el servidor");
        }
    }
}
