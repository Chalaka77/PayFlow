package com.b2b.accountservice.api.dto;

import com.B2B.extra.Currency;

import java.math.BigDecimal;

public record AddBalanceResponse(Currency currency, BigDecimal balance)
{
}
