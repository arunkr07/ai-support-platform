package com.arun.aisupportplatform.exception;

public record ErrorResponse(
        int status,
        String message
) {
}