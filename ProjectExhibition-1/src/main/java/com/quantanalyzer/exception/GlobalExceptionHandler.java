package com.quantanalyzer.exception;


import com.quantanalyzer.dto.ErrorResponse;
import com.quantanalyzer.strategy.StrategyFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(TickerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTickerNotFound(TickerNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(StrategyFactor.InvalidStrategyException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStrategy(StrategyFactor.InvalidStrategyException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    @ExceptionHandler(com.quantanalyzer.client.AlphaVantageClient.ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            com.quantanalyzer.client.AlphaVantageClient.ExternalApiException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
