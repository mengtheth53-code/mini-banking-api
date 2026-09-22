package com.minibanking.api.dto;

import java.time.Instant;

public record HealthResponse(
    String status,
    String service,
    String environment,
    String version,
    Instant timestamp
) {}
