package com.b2b.accountservice.api.dto;

import com.B2B.extra.AccountStatus;

import java.util.List;
import java.util.UUID;

public record AccountResponse(UUID accountId, String email, AccountStatus status, List<AccountBalanceResponse> saldi)
{
}
