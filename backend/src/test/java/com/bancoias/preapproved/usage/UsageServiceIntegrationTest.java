package com.bancoias.preapproved.usage;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@SpringBootTest(properties = "spring.r2dbc.url=r2dbc:h2:mem:///testdb;DB_CLOSE_DELAY=-1")
class UsageServiceIntegrationTest {

    @Autowired
    private UsageService usageService;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void resetDatabase() {
        StepVerifier.create(databaseClient.sql("DELETE FROM usage_request").then()
                        .then(databaseClient.sql("""
                                        UPDATE pre_approved
                                        SET available_amount = CASE id
                                            WHEN 'PRA-1001' THEN 1000000.00
                                            WHEN 'PRA-1002' THEN 800000.00
                                            WHEN 'PRA-2001' THEN 2000000.00
                                        END
                                        """).then()))
                .verifyComplete();
    }

    @Test
    void authorizesAndDebitsAvailableAmount() {
        UsageCommand command = command("REF-AUTH", "PRA-1001", "USR-10", "600000");

        StepVerifier.create(usageService.process(command))
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(RequestStatus.AUTHORIZED);
                    assertThat(result.remainingAmount()).isEqualByComparingTo("400000");
                    assertThat(result.processedAt()).isNotNull();
                })
                .verifyComplete();

        assertBalance("PRA-1001", "400000");
    }

    @Test
    void rejectsBlockedPreApprovedWithoutDebiting() {
        UsageCommand command = command("REF-BLOCKED", "PRA-1002", "USR-10", "100000");

        StepVerifier.create(usageService.process(command))
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(RequestStatus.REJECTED);
                    assertThat(result.rejectionReason()).contains("habilitado");
                })
                .verifyComplete();

        assertBalance("PRA-1002", "800000");
    }

    @Test
    void returnsOriginalResultForAnIdenticalReference() {
        UsageCommand command = command("REF-IDEMPOTENT", "PRA-1001", "USR-10", "200000");

        Mono<UsageResponse> scenario = usageService.process(command)
                .then(usageService.process(command));

        StepVerifier.create(scenario)
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(RequestStatus.AUTHORIZED);
                    assertThat(result.replayed()).isTrue();
                    assertThat(result.remainingAmount()).isEqualByComparingTo("800000");
                })
                .verifyComplete();

        assertBalance("PRA-1001", "800000");
    }

    @Test
    void rejectsAReferenceReusedWithDifferentData() {
        UsageCommand original = command("REF-CONFLICT", "PRA-1001", "USR-10", "100000");
        UsageCommand changed = command("REF-CONFLICT", "PRA-1001", "USR-10", "200000");

        StepVerifier.create(usageService.process(original).then(usageService.process(changed)))
                .expectError(ReferenceConflictException.class)
                .verify();

        assertBalance("PRA-1001", "900000");
    }

    @Test
    void authorizesAtMostOneCompetingRequest() {
        UsageCommand first = command("REF-CONCURRENT-1", "PRA-1001", "USR-10", "700000");
        UsageCommand second = command("REF-CONCURRENT-2", "PRA-1001", "USR-10", "700000");

        Mono<List<UsageResponse>> concurrentResults = Flux.merge(
                        usageService.process(first).subscribeOn(Schedulers.parallel()),
                        usageService.process(second).subscribeOn(Schedulers.parallel()))
                .collectList();

        StepVerifier.create(concurrentResults)
                .assertNext(results -> {
                    assertThat(results).hasSize(2);
                    assertThat(results.stream()
                                    .filter(result -> result.status() == RequestStatus.AUTHORIZED))
                            .hasSize(1);
                    assertThat(results.stream()
                                    .filter(result -> result.status() == RequestStatus.REJECTED))
                            .hasSize(1);
                })
                .verifyComplete();

        assertBalance("PRA-1001", "300000");
    }

    private UsageCommand command(
            String reference, String preApprovedId, String customerId, String amount) {
        return new UsageCommand(reference, preApprovedId, customerId, new BigDecimal(amount));
    }

    private void assertBalance(String preApprovedId, String expected) {
        StepVerifier.create(databaseClient.sql("""
                                SELECT available_amount
                                FROM pre_approved
                                WHERE id = :id
                                """)
                        .bind("id", preApprovedId)
                        .map((row, metadata) -> row.get("available_amount", BigDecimal.class))
                        .one())
                .assertNext(balance -> assertThat(balance).isEqualByComparingTo(expected))
                .verifyComplete();
    }
}
