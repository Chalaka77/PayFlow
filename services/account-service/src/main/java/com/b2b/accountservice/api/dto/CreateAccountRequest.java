package com.b2b.accountservice.api.dto;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(@NotNull String email)
{
}
