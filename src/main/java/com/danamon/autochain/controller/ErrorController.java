package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ErrorController {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<DataResponse<String>> apiException(ResponseStatusException e){
        return ResponseEntity.status(e.getStatusCode()).body(
                DataResponse.<String>builder()
                        .statusCode(e.getStatusCode().value())
                        .message(e.getReason())
                        .build()
        );
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<DataResponse<?>> dataIntegrity(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                DataResponse
                        .builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("there is an server error")
                        .data(e.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<DataResponse<?>> accsessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                DataResponse
                        .builder()
                        .statusCode(HttpStatus.FORBIDDEN.value())
                        .message("Not allowed accsess!")
                        .data(e.getMessage())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataResponse<?>> handleGenericException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                DataResponse
                        .builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("there is an server error")
                        .data(e.getLocalizedMessage())
                        .build()
        );
    }
}
