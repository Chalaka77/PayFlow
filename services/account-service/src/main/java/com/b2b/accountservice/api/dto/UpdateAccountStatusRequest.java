package com.b2b.accountservice.api.dto;

import com.B2B.extra.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(@NotNull AccountStatus accountStatus)
{
}
