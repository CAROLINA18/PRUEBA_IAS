package com.bancoias.preapproved.credit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/pre-approved")
public class PreApprovedController {

    private final PreApprovedStore preApprovedStore;

    public PreApprovedController(PreApprovedStore preApprovedStore) {
        this.preApprovedStore = preApprovedStore;
    }

    @GetMapping
    public Flux<PreApprovedResponse> findAll() {
        return preApprovedStore.findAll().map(PreApprovedResponse::from);
    }
}
