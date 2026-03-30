package com.b2b.paymentservice.handler;

import com.B2B.exceptions.BaseException;
import com.B2B.exceptions.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler
{
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex)
    {
        logger.error("Business exception: {}", ex.getMessage());
        ErrorResponse response = new ErrorResponse(Instant.now(), ex.getStatus(), ex.getMessage());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex)
    {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        ErrorResponse response = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST, message);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex)
    {
        ErrorResponse response = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST, "Malformed JSON or invalid field type.");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFound(PaymentNotFoundException ex)
    {
        logger.error("Payment not found with the provided id: {}", ex.getMessage(), ex);
        ErrorResponse response = new ErrorResponse(Instant.now(), HttpStatus.NOT_FOUND, "No payment found with this ID.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

}
