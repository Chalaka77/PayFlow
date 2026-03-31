package com.b2b.accountservice.exceptions;

import com.B2B.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class AccountNotFoundException extends BaseException
{
    public AccountNotFoundException(String message)
    {
        super(message, HttpStatus.NOT_FOUND);
    }
}
