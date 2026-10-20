package com.bancoias.preapproved.api;

import java.math.BigDecimal;

public record UsageCommand(
        String requestReference,
        String preApprovedId,
        String customerId,
        BigDecimal amount) {
}

