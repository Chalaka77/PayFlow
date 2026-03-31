package com.b2b.accountservice.exceptions;

import com.B2B.exceptions.BaseException;
import org.springframework.http.HttpStatus;

public class AccountNotActiveException extends BaseException
{
    public AccountNotActiveException(String message)
    {
        super(message, HttpStatus.NOT_ACCEPTABLE);
    }
}
