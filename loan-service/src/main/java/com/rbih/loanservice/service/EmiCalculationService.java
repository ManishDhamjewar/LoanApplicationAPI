package com.rbih.loanservice.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Calculates EMI using the standard formula:
 * EMI = P * r * (1+r)^n / ((1+r)^n - 1)
 *
 * Where:
 *   P = principal (loan amount)
 *   r = monthly interest rate (annual rate / 12 / 100)
 *   n = tenure in months
 *
 * Uses BigDecimal with scale=2 and RoundingMode=HALF_UP as required.
 */
@Service
public class EmiCalculationService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    private static final int HIGH_PRECISION_SCALE = 10;

    /**
     * Calculates the monthly EMI.
     *
     * @param principal      loan amount
     * @param annualRatePercent annual interest rate in percent (e.g. 13.5 for 13.5%)
     * @param tenureMonths   number of months
     * @return EMI rounded to 2 decimal places
     */
    public BigDecimal calculateEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        // r = annualRate / 12 / 100
        BigDecimal monthlyRate = annualRatePercent
                .divide(BigDecimal.valueOf(12), HIGH_PRECISION_SCALE, ROUNDING_MODE)
                .divide(BigDecimal.valueOf(100), HIGH_PRECISION_SCALE, ROUNDING_MODE);

        // (1 + r)^n  — use MathContext for precision in pow
        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal onePlusRPowN = onePlusR.pow(tenureMonths, new MathContext(HIGH_PRECISION_SCALE, ROUNDING_MODE));

        // EMI = P * r * (1+r)^n / ((1+r)^n - 1)
        BigDecimal numerator   = principal.multiply(monthlyRate).multiply(onePlusRPowN);
        BigDecimal denominator = onePlusRPowN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculates total amount payable over the full tenure.
     *
     * @param emi          monthly EMI
     * @param tenureMonths number of months
     * @return total payable rounded to 2 decimal places
     */
    public BigDecimal calculateTotalPayable(BigDecimal emi, int tenureMonths) {
        return emi.multiply(BigDecimal.valueOf(tenureMonths)).setScale(SCALE, ROUNDING_MODE);
    }
}
