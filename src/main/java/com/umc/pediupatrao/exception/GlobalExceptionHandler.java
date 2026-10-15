package com.umc.pediupatrao.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;


import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DominioException.class)
    public ResponseEntity<Map<String, String>> handleRegra(DominioException e) {
        Map<String, String> body = new HashMap<>();
        body.put("erro", e.getClass().getSimpleName());
        body.put("mensagem", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(RecursoNaoEncontradoException e) {
        Map<String, String> body = new HashMap<>();
        body.put("erro", "NAO_ENCONTRADO");
        body.put("mensagem", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
        Map<String, String> body = new HashMap<>();
        body.put("erro", "ACESSO_NEGADO");
        body.put("mensagem", "Você não tem permissão para esta operação.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("erro", "VALIDACAO");
        Map<String, String> campos = new HashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(f -> campos.put(f.getField(), f.getDefaultMessage()));
        body.put("campos", campos);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "erro", "PARAMETRO_INVALIDO",
            "mensagem", "Parâmetro inválido: " + e.getName()));
}

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "erro", "CORPO_INVALIDO",
            "mensagem", "Corpo da requisição inválido ou malformado."));
}

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(ConstraintViolationException e) {
        Map<String, String> campos = new HashMap<>();
        e.getConstraintViolations().forEach(v -> campos.put(v.getPropertyPath().toString(), v.getMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "erro", "VALIDACAO", "campos", campos));
    }
}
