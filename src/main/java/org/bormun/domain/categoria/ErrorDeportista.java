package org.bormun.domain.categoria;

import org.bormun.domain.participante.Deportista;

public class ErrorDeportista extends RuntimeException {
    private final Deportista deportista;
    private final MotivoErrorDeportista motivoErrorDeportista;

    public ErrorDeportista(Deportista deportista, MotivoErrorDeportista motivoErrorDeportista) {
        this.deportista = deportista;
        this.motivoErrorDeportista = motivoErrorDeportista;
    }

    public Deportista getDeportista() {
        return deportista;
    }

    public MotivoErrorDeportista getMotivoError() {
        return motivoErrorDeportista;
    }
}
