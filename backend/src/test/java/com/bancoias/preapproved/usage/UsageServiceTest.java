package com.bancoias.preapproved.usage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;

import com.bancoias.preapproved.credit.PreApprovedStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class UsageServiceTest {

    @Mock
    private UsageRequestRepository requestRepository;

    @Mock
    private PreApprovedStore preApprovedStore;

    @Mock
    private ReactiveTransactionManager transactionManager;

    private UsageService usageService;

    @BeforeEach
    void setUp() {
        usageService = new UsageService(requestRepository, preApprovedStore, transactionManager);
    }

    @Test
    void replaysAnExistingRequestWithTheSamePayload() {
        UsageRequestEntity existing = existingRequest();
        when(requestRepository.findByRequestReference("REF-UNIT"))
                .thenReturn(Mono.just(existing));

        StepVerifier.create(usageService.process(command("100000")))
                .assertNext(response -> {
                    assertThat(response.status()).isEqualTo(RequestStatus.AUTHORIZED);
                    assertThat(response.replayed()).isTrue();
                    assertThat(response.remainingAmount()).isEqualByComparingTo("900000");
                })
                .verifyComplete();

        verifyNoInteractions(preApprovedStore);
    }

    @Test
    void reportsAConflictWhenTheReferenceHasDifferentData() {
        when(requestRepository.findByRequestReference("REF-UNIT"))
                .thenReturn(Mono.just(existingRequest()));

        StepVerifier.create(usageService.process(command("200000")))
                .expectError(ReferenceConflictException.class)
                .verify();

        verifyNoInteractions(preApprovedStore);
    }

    @Test
    void reportsNotFoundWhenAReferenceDoesNotExist() {
        when(requestRepository.findByRequestReference("UNKNOWN"))
                .thenReturn(Mono.empty());

        StepVerifier.create(usageService.findByReference(" UNKNOWN "))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void rejectsABlankReferenceBeforeCallingPersistence() {
        StepVerifier.create(usageService.process(new UsageCommand(" ", "PRA-1001", "USR-10", BigDecimal.ONE)))
                .expectError(BadRequestException.class)
                .verify();

        verifyNoInteractions(requestRepository, preApprovedStore);
    }

    private UsageCommand command(String amount) {
        return new UsageCommand("REF-UNIT", "PRA-1001", "USR-10", new BigDecimal(amount));
    }

    private UsageRequestEntity existingRequest() {
        return new UsageRequestEntity(
                1L,
                "REF-UNIT",
                "PRA-1001",
                "USR-10",
                new BigDecimal("100000"),
                RequestStatus.AUTHORIZED,
                null,
                Instant.parse("2026-10-19T20:00:00Z"),
                new BigDecimal("900000"));
    }
}
