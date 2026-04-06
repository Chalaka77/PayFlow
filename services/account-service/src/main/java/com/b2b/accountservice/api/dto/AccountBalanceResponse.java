package com.b2b.accountservice.api.dto;

import com.B2B.extra.Currency;

import java.math.BigDecimal;

public record AccountBalanceResponse(Currency currency, BigDecimal balance)
{
}
