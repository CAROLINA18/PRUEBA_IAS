package com.bancoias.preapproved.usage;

import java.math.BigDecimal;
import java.time.Instant;

public record UsageResponse(
        String requestReference,
        String preApprovedId,
        String customerId,
        BigDecimal amount,
        RequestStatus status,
        String rejectionReason,
        Instant processedAt,
        BigDecimal remainingAmount,
        boolean replayed) {

    public static UsageResponse from(UsageRequestEntity entity, boolean replayed) {
        return new UsageResponse(
                entity.requestReference(),
                entity.preApprovedId(),
                entity.customerId(),
                entity.amount(),
                entity.status(),
                entity.rejectionReason(),
                entity.processedAt(),
                entity.remainingAmount(),
                replayed);
    }
}
