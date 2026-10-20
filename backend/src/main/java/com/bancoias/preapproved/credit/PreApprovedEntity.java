package com.bancoias.preapproved.credit;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("pre_approved")
public record PreApprovedEntity(
        @Id String id,
        String customerId,
        String status,
        BigDecimal availableAmount) {
}
