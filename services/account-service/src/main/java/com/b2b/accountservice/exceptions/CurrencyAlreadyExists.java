package com.b2b.accountservice.exceptions;

import com.B2B.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class CurrencyAlreadyExists extends BaseException
{

    public CurrencyAlreadyExists(String message)
    {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
