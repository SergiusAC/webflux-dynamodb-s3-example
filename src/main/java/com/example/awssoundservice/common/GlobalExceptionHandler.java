package com.example.awssoundservice.common;

import com.example.awssoundservice.response.GeneralResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GeneralResponse<Void>> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.internalServerError().body(new GeneralResponse<>(
                500, ex.getMessage(), null
        ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<GeneralResponse<Void>> handleResponseStatusException(ResponseStatusException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(ex.getStatusCode()).body(new GeneralResponse<>(
                ex.getStatusCode().value(), ex.getMessage(), null
        ));
    }

}
