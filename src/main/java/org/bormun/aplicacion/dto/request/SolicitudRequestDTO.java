package org.bormun.aplicacion.dto.request;

public record SolicitudRequestDTO(
    String nombreOrganizacion,
    EquipoRequestDTO equipo,
    int idCategoria
) {
}

//TODO: CREAR LOS DTOS DE RESPONSE Y REQUEST PARA DESPUES CREAR CONTROLADORES CAPACES DE CREAR EVENTOS E INSCRIBIR PERSONAS A DICHOS EVENTOS