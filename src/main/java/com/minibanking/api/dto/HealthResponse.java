package com.minibanking.api.dto;

import java.time.Instant;

/**
 * Immutable response object using Java 21 Record.
 * Automatically generates constructor, getters, equals, hashCode, and toString.
 */
public record HealthResponse(
    String status,
    String service,
    String environment,
    String version,
    Instant timestamp
) {}
