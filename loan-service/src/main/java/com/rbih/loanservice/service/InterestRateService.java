package com.rbih.loanservice.service;

import com.rbih.loanservice.domain.enums.EmploymentType;
import com.rbih.loanservice.domain.enums.RiskBand;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Calculates the final interest rate using the formula:
 * Final Rate = Base Rate (12%) + Risk Premium + Employment Premium + Loan Size Premium
 */
@Service
public class InterestRateService {

    private static final BigDecimal BASE_RATE = new BigDecimal("12.0");

    // Risk Premiums
    private static final BigDecimal RISK_PREMIUM_LOW    = BigDecimal.ZERO;
    private static final BigDecimal RISK_PREMIUM_MEDIUM = new BigDecimal("1.5");
    private static final BigDecimal RISK_PREMIUM_HIGH   = new BigDecimal("3.0");

    // Employment Premiums
    private static final BigDecimal EMPLOYMENT_PREMIUM_SALARIED      = BigDecimal.ZERO;
    private static final BigDecimal EMPLOYMENT_PREMIUM_SELF_EMPLOYED  = new BigDecimal("1.0");

    // Loan Size Premium threshold and value
    private static final BigDecimal LARGE_LOAN_THRESHOLD = new BigDecimal("1000000");
    private static final BigDecimal LOAN_SIZE_PREMIUM    = new BigDecimal("0.5");

    public BigDecimal calculate(RiskBand riskBand, EmploymentType employmentType, BigDecimal loanAmount) {
        BigDecimal riskPremium       = resolveRiskPremium(riskBand);
        BigDecimal employmentPremium = resolveEmploymentPremium(employmentType);
        BigDecimal loanSizePremium   = resolveLoanSizePremium(loanAmount);

        return BASE_RATE
                .add(riskPremium)
                .add(employmentPremium)
                .add(loanSizePremium);
    }

    private BigDecimal resolveRiskPremium(RiskBand riskBand) {
        return switch (riskBand) {
            case LOW    -> RISK_PREMIUM_LOW;
            case MEDIUM -> RISK_PREMIUM_MEDIUM;
            case HIGH   -> RISK_PREMIUM_HIGH;
        };
    }

    private BigDecimal resolveEmploymentPremium(EmploymentType employmentType) {
        return switch (employmentType) {
            case SALARIED      -> EMPLOYMENT_PREMIUM_SALARIED;
            case SELF_EMPLOYED -> EMPLOYMENT_PREMIUM_SELF_EMPLOYED;
        };
    }

    private BigDecimal resolveLoanSizePremium(BigDecimal loanAmount) {
        return loanAmount.compareTo(LARGE_LOAN_THRESHOLD) > 0
                ? LOAN_SIZE_PREMIUM
                : BigDecimal.ZERO;
    }
}
