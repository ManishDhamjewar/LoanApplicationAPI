package com.rbih.loanservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoanApplicationRequest {

    @NotNull(message = "Applicant information is required")
    @Valid
    private ApplicantDTO applicant;

    @NotNull(message = "Loan information is required")
    @Valid
    private LoanDTO loan;
}
