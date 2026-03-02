package com.ars.core.infrastructure.web.error;

import org.springframework.http.HttpStatus;

public class InternalServerException extends AppException {
    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message, cause);
    }
}
