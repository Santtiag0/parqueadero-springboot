package com.parqueadero.exception;

public class ConflictoDeNegocioException extends RuntimeException {

    public ConflictoDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
