package com.b2b.accountservice.handler;

import com.B2B.exceptions.BaseException;
import com.B2B.exceptions.dto.ErrorResponse;
import com.b2b.accountservice.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex)
    {
        LOGGER.error("Business Exception: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(Instant.now(), ex.getStatus().value(), ex.getMessage());

        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex)
    {
        LOGGER.error("Data integrity violation: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(Instant.now(), 409, "Email already in use");
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex)
    {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ErrorResponse response = new ErrorResponse(Instant.now(), 400, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotActiveException(AccountNotActiveException ex)
    {
        LOGGER.error("Account not active Exception: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(Instant.now(), ex.getStatus().value(), ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFoundException(AccountNotFoundException ex)
    {
        LOGGER.error("Account not found Exception: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(Instant.now(), ex.getStatus().value(), ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CurrencyAlreadyExists.class)
    public ResponseEntity<ErrorResponse> handleCurrencyAlreadyExists(CurrencyAlreadyExists ex)
    {
        LOGGER.error("Currency already exists Exception: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(Instant.now(), ex.getStatus().value(), ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CurrencyNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyNotSupportedException(CurrencyNotSupportedException ex)
    {
        LOGGER.error("Currency not supported Exception: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(Instant.now(), ex.getStatus().value(), ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex)
    {
        LOGGER.error("Insufficient funds Exception: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(Instant.now(), ex.getStatus().value(), ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
