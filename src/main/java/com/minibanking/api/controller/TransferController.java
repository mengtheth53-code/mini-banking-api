package com.minibanking.api.controller;

import com.minibanking.api.dto.request.TransferRequest;
import com.minibanking.api.dto.response.TransactionResponse;
import com.minibanking.api.security.CustomUserPrincipal;
import com.minibanking.api.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transfers")
@Tag(name = "Fund Transfers", description = "Atomic money transfers between accounts with @Transactional guarantees")
@SecurityRequirement(name = "Bearer Authentication")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @Operation(summary = "Transfer money between two bank accounts (Atomic & Rollback-safe)")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        TransactionResponse response = transferService.transfer(request, principal);
        return ResponseEntity.ok(response);
    }
}
