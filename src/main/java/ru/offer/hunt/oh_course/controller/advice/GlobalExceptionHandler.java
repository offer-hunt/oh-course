package ru.offer.hunt.oh_course.controller.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.offer.hunt.oh_course.exception.StatsServiceConnectionException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(StatsServiceConnectionException.class)
    public ResponseEntity<Object> handleStatsConnectionError(StatsServiceConnectionException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Service Unavailable",
                        "message", ex.getMessage()
                ));
    }
}