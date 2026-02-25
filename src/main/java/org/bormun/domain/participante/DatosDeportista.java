package org.bormun.domain.participante;

import org.bormun.domain.evento.GeneroNacimiento;

import java.time.LocalDate;

public record DatosDeportista(String nombre, String identificacion, GeneroNacimiento generoNacimiento, LocalDate fechaNacimiento) {
}