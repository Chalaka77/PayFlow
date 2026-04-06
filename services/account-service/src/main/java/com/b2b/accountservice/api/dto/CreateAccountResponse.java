package com.b2b.accountservice.api.dto;

import com.B2B.extra.AccountStatus;

public record CreateAccountResponse(String email, AccountStatus status)
{
}
