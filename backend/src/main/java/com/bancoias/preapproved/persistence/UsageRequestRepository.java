package com.bancoias.preapproved.persistence;

import com.bancoias.preapproved.domain.UsageRequestEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UsageRequestRepository extends ReactiveCrudRepository<UsageRequestEntity, Long> {

    Mono<UsageRequestEntity> findByRequestReference(String requestReference);

    @Query("SELECT * FROM usage_request ORDER BY processed_at DESC LIMIT 20")
    Flux<UsageRequestEntity> findRecent();
}

