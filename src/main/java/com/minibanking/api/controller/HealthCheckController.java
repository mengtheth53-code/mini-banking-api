package com.minibanking.api.controller;

import com.minibanking.api.dto.HealthResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/health")
public class HealthCheckController {

    @Value("${spring.application.name:mini-banking-api}")
    private String serviceName;

    @Value("${app.environment:development}")
    private String environment;

    @Value("${app.version:1.0.0}")
    private String version;

    @GetMapping
    public ResponseEntity<HealthResponse> checkHealth() {
        HealthResponse response = new HealthResponse(
            "UP",
            serviceName,
            environment,
            version,
            Instant.now()
        );
        return ResponseEntity.ok(response);
    }
}
