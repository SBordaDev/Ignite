package org.bormun.presentacion.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitudRequestDTO(
    @NotBlank
        String nombreOrganizacion,

    @NotNull
    @Valid
    EquipoRequestDTO equipo,

    @NotNull
    int idCategoria
) {
}

//TODO: CREAR LOS DTOS DE RESPONSE Y REQUEST PARA DESPUES CREAR CONTROLADORES CAPACES DE CREAR EVENTOS E INSCRIBIR PERSONAS A DICHOS EVENTOS