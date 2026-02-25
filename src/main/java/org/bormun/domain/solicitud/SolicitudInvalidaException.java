package org.bormun.domain.solicitud;

import org.bormun.domain.participante.Deportista;

import java.util.List;

public class SolicitudInvalidaException extends RuntimeException {
    private List<Deportista> deportistasCulpables;

    public SolicitudInvalidaException(String message){
        super(message);
    }

    public SolicitudInvalidaException(String message, List<Deportista> deportistas) {
        super(message);
        this.deportistasCulpables = deportistas;
    }

    public List<Deportista> getDeportistasCulpables() {
        return deportistasCulpables;
    }
}
