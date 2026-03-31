package com.b2b.accountservice.exceptions;

import com.B2B.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends BaseException
{

    public InsufficientFundsException(String message)
    {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
