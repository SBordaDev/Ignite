package org.bormun.evento;

import org.bormun.dominio.excepciones.ErrorDeportista;
import org.bormun.dominio.excepciones.SolicitudInvalidaException;
import org.bormun.dominio.modelos.DatosDeportista;
import org.bormun.dominio.modelos.Deportista;
import org.bormun.dominio.modelos.GeneroNacimiento;
import org.bormun.dominio.modelos.MotivoErrorDeportista;
import org.bormun.presentacion.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void manejarErroresDeValidacion_DeberiaRetornar400YDetalles() throws Exception {
        mockMvc.perform(get("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Error en los datos enviados"))
                .andExpect(jsonPath("$.detalles.campoPrueba").value("El campo no puede estar vacío"));
    }

    @Test
    void manejarSolicitudInvalida_SinCulpables_DeberiaRetornar400SinLista() throws Exception {
        mockMvc.perform(get("/test/dominio-simple")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El deportista ya está inscrito en otra categoría"))
                .andExpect(jsonPath("$.detalles").doesNotExist());
    }

    @Test
    void manejarSolicitudInvalida_ConCulpables_DeberiaSerializarDeportistas() throws Exception {
        mockMvc.perform(get("/test/dominio-con-culpables")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("La solicitud contiene errores de negocio"))
                .andExpect(jsonPath("$.detalles[0].motivoError").value("GENERO_INVALIDO"))
                .andExpect(jsonPath("$.detalles[0].deportista.nombre").value("Atleta Caos"));
    }

    @Test
    void manejarErrorDeportista_Directo_DeberiaRetornarDetalleDeportista() throws Exception {
        mockMvc.perform(get("/test/error-deportista")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Regla de negocio deportiva violada"))
                .andExpect(jsonPath("$.detalles.motivoError").value("EDAD_INVALIDA"))
                .andExpect(jsonPath("$.detalles.deportista.nombre").value("Atleta Edad"));
    }

    @Test
    void manejarIlegalArgumentException_DeberiaRetornar400() throws Exception {
        mockMvc.perform(get("/test/ilegal")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Argumento inválido o recurso no encontrado"));
    }

    // Controlador Dummy exclusivo para simular los disparos de excepciones
    @RestController
    static class TestController {

        @GetMapping("/test/validation")
        public void lanzarValidacion() throws MethodArgumentNotValidException, NoSuchMethodException {
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objetoPrueba");
            bindingResult.addError(new FieldError("objetoPrueba", "campoPrueba", "El campo no puede estar vacío"));

            // Obtenemos una referencia real a este mismo método usando Reflexión
            java.lang.reflect.Method method = this.getClass().getMethod("lanzarValidacion");
            MethodParameter parametroReal = new MethodParameter(method, -1);

            throw new MethodArgumentNotValidException(
                    parametroReal,
                    bindingResult
            );
        }

        @GetMapping("/test/ilegal")
        public void lanzarIlegal() {
            throw new IllegalArgumentException("Argumento inválido o recurso no encontrado");
        }

        @GetMapping("/test/dominio-simple")
        public void lanzarDominioSimple() {
            throw new SolicitudInvalidaException("El deportista ya está inscrito en otra categoría");
        }

        @GetMapping("/test/dominio-con-culpables")
        public void lanzarDominioConCulpables() {
            // Instanciamos un deportista real (ajusta los parámetros según tu constructor de dominio)
            Deportista atleta = new Deportista(
                    new DatosDeportista(
                        "Atleta Caos",
                        "12345",
                        GeneroNacimiento.MUJER,
                        LocalDate.of(2005, 5, 23)
                    )
            );

            ErrorDeportista error = new ErrorDeportista(atleta, MotivoErrorDeportista.GENERO_INVALIDO);

            throw new SolicitudInvalidaException("La solicitud contiene errores de negocio", List.of(error));
        }

        @GetMapping("/test/error-deportista")
        public void lanzarErrorDeportistaDirecto() {
            Deportista atleta = new Deportista(
                    new DatosDeportista(
                            "Atleta Edad",
                            "98765",
                            GeneroNacimiento.HOMBRE,
                            LocalDate.of(2020, 5, 23)
                    )
            );
            throw new ErrorDeportista(atleta, MotivoErrorDeportista.EDAD_INVALIDA);
        }
    }
}