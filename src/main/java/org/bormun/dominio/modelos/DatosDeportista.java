package org.bormun.dominio.modelos;

import java.time.LocalDate;

public record DatosDeportista(String nombre, String identificacion, GeneroNacimiento generoNacimiento, LocalDate fechaNacimiento) {
}