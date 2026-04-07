package com.B2B.exceptions.dto;

import java.time.Instant;

public record ErrorResponse(Instant timestamp, int status, String message)
{}
