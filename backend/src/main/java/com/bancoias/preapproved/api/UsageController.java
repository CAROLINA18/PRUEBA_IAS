package com.bancoias.preapproved.api;

import com.bancoias.preapproved.application.UsageService;
import com.bancoias.preapproved.persistence.PreApprovedStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class UsageController {

    private final UsageService usageService;
    private final PreApprovedStore preApprovedStore;

    public UsageController(UsageService usageService, PreApprovedStore preApprovedStore) {
        this.usageService = usageService;
        this.preApprovedStore = preApprovedStore;
    }

    @PostMapping("/requests")
    public Mono<UsageResponse> process(@RequestBody UsageCommand command) {
        return usageService.process(command);
    }

    @GetMapping("/requests/{requestReference}")
    public Mono<UsageResponse> findByReference(@PathVariable String requestReference) {
        return usageService.findByReference(requestReference);
    }

    @GetMapping("/requests/recent")
    public Flux<UsageResponse> findRecent() {
        return usageService.findRecent();
    }

    @GetMapping("/pre-approved")
    public Flux<PreApprovedResponse> findPreApproved() {
        return preApprovedStore.findAll().map(PreApprovedResponse::from);
    }
}

