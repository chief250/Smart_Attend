package com.smartattend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class FaceVerificationException extends RuntimeException {
    public FaceVerificationException(String message) {
        super(message);
    }
}
