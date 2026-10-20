package com.bancoias.preapproved.api;

import java.math.BigDecimal;

import com.bancoias.preapproved.domain.PreApprovedEntity;

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

