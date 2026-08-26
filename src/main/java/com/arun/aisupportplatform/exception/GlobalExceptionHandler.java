package com.arun.aisupportplatform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTicketNotFound(
            TicketNotFoundException exception
    ) {

        ErrorResponse error = new ErrorResponse(
                404,
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(TicketAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleTicketAlreadyAssigned(
            TicketAlreadyAssignedException exception
    ) {

        ErrorResponse error = new ErrorResponse(
                409,
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }
}