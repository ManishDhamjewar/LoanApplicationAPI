package com.rbih.loanservice.dto.request;

import com.rbih.loanservice.domain.enums.LoanPurpose;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanDTO {

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "10000.00", message = "Loan amount must be at least 10,000")
    @DecimalMax(value = "5000000.00", message = "Loan amount must not exceed 50,00,000")
    private BigDecimal amount;

    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Tenure must be at least 6 months")
    @Max(value = 360, message = "Tenure must not exceed 360 months")
    private Integer tenureMonths;

    @NotNull(message = "Loan purpose is required")
    private LoanPurpose purpose;
}
