package com.parqueadero.exception;

import java.util.List;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(RecursoNoEncontradoException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return construir(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ConflictoDeNegocioException.class)
    public ResponseEntity<ErrorResponse> manejarConflicto(ConflictoDeNegocioException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> manejarBloqueoOptimista(OptimisticLockingFailureException ex) {
        return construir(HttpStatus.CONFLICT,
                "El cupo fue tomado por otra solicitud al mismo tiempo; intente de nuevo.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        List<String> detalles = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        ErrorResponse cuerpo = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Datos invalidos",
                "La peticion contiene campos invalidos", detalles);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(cuerpo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje) {
        ErrorResponse cuerpo = new ErrorResponse(status.value(), status.getReasonPhrase(), mensaje);
        return ResponseEntity.status(status).body(cuerpo);
    }
}
