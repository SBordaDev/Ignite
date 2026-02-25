package org.bormun.usecase;

public class AceptarSolicitudInvalida extends RuntimeException {
    public AceptarSolicitudInvalida(String message) {
        super(message);
    }
}
