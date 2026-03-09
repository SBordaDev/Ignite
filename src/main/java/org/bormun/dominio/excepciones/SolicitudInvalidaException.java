package org.bormun.dominio.excepciones;

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
