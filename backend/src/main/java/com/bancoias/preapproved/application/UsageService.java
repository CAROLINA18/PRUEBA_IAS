package com.bancoias.preapproved.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

import com.bancoias.preapproved.api.UsageCommand;
import com.bancoias.preapproved.api.UsageResponse;
import com.bancoias.preapproved.domain.PreApprovedEntity;
import com.bancoias.preapproved.domain.RequestStatus;
import com.bancoias.preapproved.domain.UsageRequestEntity;
import com.bancoias.preapproved.persistence.PreApprovedStore;
import com.bancoias.preapproved.persistence.UsageRequestRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UsageService {

    private static final String PRE_APPROVED_NOT_FOUND = "El preaprobado no existe";
    private static final String CUSTOMER_MISMATCH = "El preaprobado no corresponde al cliente";
    private static final String PRE_APPROVED_BLOCKED = "El preaprobado no está habilitado";
    private static final String INVALID_AMOUNT = "El valor solicitado debe ser mayor que cero";
    private static final String INSUFFICIENT_BALANCE = "El valor solicitado supera el cupo disponible";

    private final UsageRequestRepository requestRepository;
    private final PreApprovedStore preApprovedStore;
    private final TransactionalOperator transaction;

    public UsageService(
            UsageRequestRepository requestRepository,
            PreApprovedStore preApprovedStore,
            ReactiveTransactionManager transactionManager) {
        this.requestRepository = requestRepository;
        this.preApprovedStore = preApprovedStore;
        this.transaction = TransactionalOperator.create(transactionManager);
    }

    public Mono<UsageResponse> process(UsageCommand rawCommand) {
        UsageCommand command = normalize(rawCommand);
        if (command.requestReference().isBlank()) {
            return Mono.error(new BadRequestException("requestReference es obligatorio"));
        }

        return requestRepository.findByRequestReference(command.requestReference())
                .flatMap(existing -> resolveDuplicate(existing, command))
                .switchIfEmpty(Mono.defer(() -> transaction.transactional(processNew(command))))
                .onErrorResume(DataIntegrityViolationException.class,
                        ignored -> requestRepository.findByRequestReference(command.requestReference())
                                .switchIfEmpty(Mono.error(ignored))
                                .flatMap(existing -> resolveDuplicate(existing, command)));
    }

    public Mono<UsageResponse> findByReference(String reference) {
        return requestRepository.findByRequestReference(clean(reference))
                .map(entity -> UsageResponse.from(entity, false))
                .switchIfEmpty(Mono.error(new NotFoundException("No existe la solicitud " + reference)));
    }

    public Flux<UsageResponse> findRecent() {
        return requestRepository.findRecent().map(entity -> UsageResponse.from(entity, false));
    }

    private Mono<UsageResponse> processNew(UsageCommand command) {
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return saveRejected(command, INVALID_AMOUNT, null);
        }

        return preApprovedStore.findById(command.preApprovedId())
                .flatMap(preApproved -> validateAndProcess(command, preApproved))
                .switchIfEmpty(saveRejected(command, PRE_APPROVED_NOT_FOUND, null));
    }

    private Mono<UsageResponse> validateAndProcess(UsageCommand command, PreApprovedEntity preApproved) {
        if (!preApproved.customerId().equals(command.customerId())) {
            return saveRejected(command, CUSTOMER_MISMATCH, preApproved.availableAmount());
        }
        if (!"ACTIVE".equals(preApproved.status())) {
            return saveRejected(command, PRE_APPROVED_BLOCKED, preApproved.availableAmount());
        }

        return preApprovedStore
                .debitIfAvailable(preApproved.id(), command.customerId(), command.amount())
                .flatMap(updated -> updated == 1
                        ? preApprovedStore.findById(preApproved.id())
                                .flatMap(current -> saveAuthorized(command, current.availableAmount()))
                        : preApprovedStore.findById(preApproved.id())
                                .flatMap(current -> saveRejected(
                                        command, INSUFFICIENT_BALANCE, current.availableAmount())));
    }

    private Mono<UsageResponse> saveAuthorized(UsageCommand command, BigDecimal remainingAmount) {
        return save(command, RequestStatus.AUTHORIZED, null, remainingAmount);
    }

    private Mono<UsageResponse> saveRejected(
            UsageCommand command, String reason, BigDecimal remainingAmount) {
        return save(command, RequestStatus.REJECTED, reason, remainingAmount);
    }

    private Mono<UsageResponse> save(
            UsageCommand command,
            RequestStatus status,
            String reason,
            BigDecimal remainingAmount) {
        UsageRequestEntity entity = new UsageRequestEntity(
                null,
                command.requestReference(),
                command.preApprovedId(),
                command.customerId(),
                command.amount(),
                status,
                reason,
                Instant.now(),
                remainingAmount);
        return requestRepository.save(entity).map(saved -> UsageResponse.from(saved, false));
    }

    private Mono<UsageResponse> resolveDuplicate(
            UsageRequestEntity original, UsageCommand command) {
        if (samePayload(original, command)) {
            return Mono.just(UsageResponse.from(original, true));
        }
        return Mono.error(new ReferenceConflictException(
                "La referencia ya fue procesada con información diferente"));
    }

    private boolean samePayload(UsageRequestEntity original, UsageCommand command) {
        return original.preApprovedId().equals(command.preApprovedId())
                && original.customerId().equals(command.customerId())
                && sameAmount(original.amount(), command.amount());
    }

    private boolean sameAmount(BigDecimal first, BigDecimal second) {
        return first == null ? second == null : second != null && first.compareTo(second) == 0;
    }

    private UsageCommand normalize(UsageCommand command) {
        if (command == null) {
            throw new BadRequestException("El cuerpo de la solicitud es obligatorio");
        }
        return new UsageCommand(
                clean(command.requestReference()),
                clean(command.preApprovedId()),
                clean(command.customerId()),
                command.amount());
    }

    private String clean(String value) {
        return Objects.requireNonNullElse(value, "").trim();
    }
}

