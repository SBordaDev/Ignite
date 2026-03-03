package org.bormun.domain.solicitud;

import org.bormun.domain.categoria.ErrorDeportista;
import org.bormun.domain.participante.Deportista;

import java.util.List;

public class SolicitudInvalidaException extends RuntimeException {
    private List<ErrorDeportista> culpables;

    public SolicitudInvalidaException(String message){
        super(message);
    }

    public SolicitudInvalidaException(String message, List<ErrorDeportista> deportistas) {
        super(message);
        this.culpables = deportistas;
    }

    public List<ErrorDeportista> getCulpables() {
        return culpables;
    }
}
