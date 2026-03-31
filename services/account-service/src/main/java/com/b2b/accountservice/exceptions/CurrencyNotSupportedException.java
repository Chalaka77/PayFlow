package com.b2b.accountservice.exceptions;

import com.B2B.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class CurrencyNotSupportedException extends BaseException
{

    public CurrencyNotSupportedException(String message)
    {
        super(message, HttpStatus.NOT_FOUND);
    }
}
