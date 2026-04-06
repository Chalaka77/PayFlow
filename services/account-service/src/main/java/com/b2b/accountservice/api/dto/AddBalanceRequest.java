package com.b2b.accountservice.api.dto;

import com.B2B.extra.Currency;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddBalanceRequest(@NotNull Currency currency,@NotNull BigDecimal amount)
{
}
