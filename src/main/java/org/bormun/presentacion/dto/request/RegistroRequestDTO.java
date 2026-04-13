package org.bormun.presentacion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.bormun.dominio.modelos.Roles;

public record RegistroRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String password
) {}

