package com.rbih.loanservice.dto.request;

import com.rbih.loanservice.domain.enums.EmploymentType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ApplicantDTO {

    @NotBlank(message = "Applicant name is required")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 21, message = "Applicant age must be at least 21")
    @Max(value = 60, message = "Applicant age must not exceed 60")
    private Integer age;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be greater than 0")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    @NotNull(message = "Credit score is required")
    @Min(value = 300, message = "Credit score must be at least 300")
    @Max(value = 900, message = "Credit score must not exceed 900")
    private Integer creditScore;
}
