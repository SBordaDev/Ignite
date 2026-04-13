package org.bormun.presentacion.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 2. Lo que envía el usuario para iniciar sesión
public record LoginRequestDTO(
        @NotBlank @Email String email,
        @NotBlank String password
) {}
