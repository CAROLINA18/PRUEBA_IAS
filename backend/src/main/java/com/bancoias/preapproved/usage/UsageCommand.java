package com.bancoias.preapproved.usage;

import java.math.BigDecimal;

public record UsageCommand(
        String requestReference,
        String preApprovedId,
        String customerId,
        BigDecimal amount) {
}
