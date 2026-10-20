package com.bancoias.preapproved.persistence;

import java.math.BigDecimal;

import com.bancoias.preapproved.domain.PreApprovedEntity;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class PreApprovedStore {

    private final DatabaseClient databaseClient;

    public PreApprovedStore(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<PreApprovedEntity> findById(String id) {
        return databaseClient.sql("""
                        SELECT id, customer_id, status, available_amount
                        FROM pre_approved
                        WHERE id = :id
                        """)
                .bind("id", id)
                .map((row, metadata) -> new PreApprovedEntity(
                        row.get("id", String.class),
                        row.get("customer_id", String.class),
                        row.get("status", String.class),
                        row.get("available_amount", BigDecimal.class)))
                .one();
    }

    public Flux<PreApprovedEntity> findAll() {
        return databaseClient.sql("""
                        SELECT id, customer_id, status, available_amount
                        FROM pre_approved
                        ORDER BY id
                        """)
                .map((row, metadata) -> new PreApprovedEntity(
                        row.get("id", String.class),
                        row.get("customer_id", String.class),
                        row.get("status", String.class),
                        row.get("available_amount", BigDecimal.class)))
                .all();
    }

    public Mono<Long> debitIfAvailable(String id, String customerId, BigDecimal amount) {
        return databaseClient.sql("""
                        UPDATE pre_approved
                        SET available_amount = available_amount - :amount
                        WHERE id = :id
                          AND customer_id = :customerId
                          AND status = 'ACTIVE'
                          AND available_amount >= :amount
                        """)
                .bind("amount", amount)
                .bind("id", id)
                .bind("customerId", customerId)
                .fetch()
                .rowsUpdated();
    }
}

