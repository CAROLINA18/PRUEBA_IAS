package com.bancoias.preapproved.usage;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/requests")
public class UsageController {

    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @PostMapping
    public Mono<UsageResponse> process(@RequestBody UsageCommand command) {
        return usageService.process(command);
    }

    @GetMapping("/{requestReference}")
    public Mono<UsageResponse> findByReference(@PathVariable String requestReference) {
        return usageService.findByReference(requestReference);
    }

    @GetMapping("/recent")
    public Flux<UsageResponse> findRecent() {
        return usageService.findRecent();
    }
}
