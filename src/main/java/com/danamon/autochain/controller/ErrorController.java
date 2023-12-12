package com.danamon.autochain.controller;

import com.danamon.autochain.dto.DataResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DataResponse<?>> handleGenericException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                DataResponse
                        .builder()
                        .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .message("there is an server error")
                        .data(e.getMessage())
                        .build()
        );
    }
}
