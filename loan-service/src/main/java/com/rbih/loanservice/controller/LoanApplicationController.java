package com.rbih.loanservice.controller;

import com.rbih.loanservice.dto.request.LoanApplicationRequest;
import com.rbih.loanservice.dto.response.LoanApplicationResponse;
import com.rbih.loanservice.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    /**
     * POST /applications
     * Accepts a loan application, evaluates eligibility, and returns a decision.
     */
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> createApplication(
            @Valid @RequestBody LoanApplicationRequest request) {

        log.info("Received loan application request for: {}", request.getApplicant().getName());
        LoanApplicationResponse response = loanApplicationService.process(request);
        return ResponseEntity.ok(response);
    }
}
