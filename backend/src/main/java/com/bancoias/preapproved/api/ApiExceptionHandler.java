package com.bancoias.preapproved.api;

import java.time.Instant;

import com.bancoias.preapproved.application.BadRequestException;
import com.bancoias.preapproved.application.NotFoundException;
import com.bancoias.preapproved.application.ReferenceConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError badRequest(BadRequestException exception) {
        return new ApiError("BAD_REQUEST", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(ReferenceConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError conflict(ReferenceConflictException exception) {
        return new ApiError("REFERENCE_CONFLICT", exception.getMessage(), Instant.now());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFound(NotFoundException exception) {
        return new ApiError("NOT_FOUND", exception.getMessage(), Instant.now());
    }

    public record ApiError(String code, String message, Instant timestamp) {
    }
}

