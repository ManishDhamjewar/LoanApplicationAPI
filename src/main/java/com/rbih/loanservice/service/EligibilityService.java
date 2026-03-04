package com.rbih.loanservice.service;

import com.rbih.loanservice.domain.EligibilityResult;
import com.rbih.loanservice.dto.request.ApplicantDTO;
import com.rbih.loanservice.dto.request.LoanDTO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Evaluates applicant eligibility based on the three business rules:
 *  1. Credit score must be >= 600
 *  2. Age + tenure (in years) must not exceed 65
 *  3. EMI must not exceed 60% of monthly income
 *
 * Note: Rule 3 here uses 60% — the 50% threshold is applied during offer generation.
 */
@Service
public class EligibilityService {

    private static final int MIN_CREDIT_SCORE = 600;
    private static final int MAX_AGE_PLUS_TENURE_YEARS = 65;
    private static final BigDecimal EMI_TO_INCOME_LIMIT_PERCENT = new BigDecimal("0.60");

    /**
     * Runs all eligibility checks and returns a combined result.
     * All checks are run (not short-circuited) so the applicant receives all rejection reasons at once.
     */
    public EligibilityResult evaluate(ApplicantDTO applicant, LoanDTO loan, BigDecimal emi) {
        List<String> reasons = new ArrayList<>();

        if (isCreditScoreTooLow(applicant.getCreditScore())) {
            reasons.add("CREDIT_SCORE_TOO_LOW");
        }

        if (isAgeTenureLimitExceeded(applicant.getAge(), loan.getTenureMonths())) {
            reasons.add("AGE_TENURE_LIMIT_EXCEEDED");
        }

        if (isEmiExceeds60Percent(emi, applicant.getMonthlyIncome())) {
            reasons.add("EMI_EXCEEDS_60_PERCENT");
        }

        return reasons.isEmpty()
                ? EligibilityResult.approved()
                : EligibilityResult.rejected(reasons);
    }

    boolean isCreditScoreTooLow(int creditScore) {
        return creditScore < MIN_CREDIT_SCORE;
    }

    boolean isAgeTenureLimitExceeded(int age, int tenureMonths) {
        // Tenure converted to years (ceiling to be conservative)
        int tenureYears = (int) Math.ceil(tenureMonths / 12.0);
        return (age + tenureYears) > MAX_AGE_PLUS_TENURE_YEARS;
    }

    boolean isEmiExceeds60Percent(BigDecimal emi, BigDecimal monthlyIncome) {
        BigDecimal maxAllowedEmi = monthlyIncome.multiply(EMI_TO_INCOME_LIMIT_PERCENT)
                .setScale(2, RoundingMode.HALF_UP);
        return emi.compareTo(maxAllowedEmi) > 0;
    }
}
