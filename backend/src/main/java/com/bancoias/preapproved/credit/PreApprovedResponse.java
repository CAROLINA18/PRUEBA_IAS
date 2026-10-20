package com.bancoias.preapproved.credit;

import java.math.BigDecimal;

public record PreApprovedResponse(
        String id,
        String customerId,
        String status,
        BigDecimal availableAmount) {

    public static PreApprovedResponse from(PreApprovedEntity entity) {
        return new PreApprovedResponse(
                entity.id(), entity.customerId(), entity.status(), entity.availableAmount());
    }
}
