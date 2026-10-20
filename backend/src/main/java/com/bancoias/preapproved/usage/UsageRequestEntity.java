package com.bancoias.preapproved.usage;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("USAGE_REQUEST")
public record UsageRequestEntity(
        @Id Long id,
        String requestReference,
        String preApprovedId,
        String customerId,
        BigDecimal amount,
        RequestStatus status,
        String rejectionReason,
        Instant processedAt,
        BigDecimal remainingAmount) {
}
