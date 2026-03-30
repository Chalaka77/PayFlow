package com.b2b.paymentservice.handler;

import com.B2B.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class PaymentNotFoundException extends BaseException
{

    public PaymentNotFoundException(String message)
    {
        super(message, HttpStatus.NOT_FOUND);
    }
}
