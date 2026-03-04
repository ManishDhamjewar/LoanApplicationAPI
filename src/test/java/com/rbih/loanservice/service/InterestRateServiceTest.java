package com.rbih.loanservice.service;

import com.rbih.loanservice.domain.enums.EmploymentType;
import com.rbih.loanservice.domain.enums.RiskBand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Interest Rate Service Tests")
class InterestRateServiceTest {

    private InterestRateService interestRateService;

    @BeforeEach
    void setUp() {
        interestRateService = new InterestRateService();
    }

    @Test
    @DisplayName("LOW risk + SALARIED + small loan = 12% base only")
    void lowRiskSalariedSmallLoan() {
        BigDecimal rate = interestRateService.calculate(
                RiskBand.LOW, EmploymentType.SALARIED, new BigDecimal("500000"));
        assertThat(rate).isEqualByComparingTo(new BigDecimal("12.0"));
    }

    @Test
    @DisplayName("MEDIUM risk + SALARIED + small loan = 13.5%")
    void mediumRiskSalariedSmallLoan() {
        BigDecimal rate = interestRateService.calculate(
                RiskBand.MEDIUM, EmploymentType.SALARIED, new BigDecimal("500000"));
        assertThat(rate).isEqualByComparingTo(new BigDecimal("13.5"));
    }

    @Test
    @DisplayName("HIGH risk + SALARIED + small loan = 15%")
    void highRiskSalariedSmallLoan() {
        BigDecimal rate = interestRateService.calculate(
                RiskBand.HIGH, EmploymentType.SALARIED, new BigDecimal("500000"));
        assertThat(rate).isEqualByComparingTo(new BigDecimal("15.0"));
    }

    @Test
    @DisplayName("LOW risk + SELF_EMPLOYED + small loan = 13%")
    void lowRiskSelfEmployedSmallLoan() {
        BigDecimal rate = interestRateService.calculate(
                RiskBand.LOW, EmploymentType.SELF_EMPLOYED, new BigDecimal("500000"));
        assertThat(rate).isEqualByComparingTo(new BigDecimal("13.0"));
    }

    @Test
    @DisplayName("LOW risk + SALARIED + large loan (>10L) = 12.5%")
    void lowRiskSalariedLargeLoan() {
        BigDecimal rate = interestRateService.calculate(
                RiskBand.LOW, EmploymentType.SALARIED, new BigDecimal("1500000"));
        assertThat(rate).isEqualByComparingTo(new BigDecimal("12.5"));
    }

    @Test
    @DisplayName("HIGH risk + SELF_EMPLOYED + large loan = 16.5%")
    void highRiskSelfEmployedLargeLoan() {
        // 12 + 3 + 1 + 0.5 = 16.5
        BigDecimal rate = interestRateService.calculate(
                RiskBand.HIGH, EmploymentType.SELF_EMPLOYED, new BigDecimal("2000000"));
        assertThat(rate).isEqualByComparingTo(new BigDecimal("16.5"));
    }

    @Test
    @DisplayName("Loan of exactly 10L should NOT attract size premium")
    void loanAtExactlyThresholdNoSizePremium() {
        BigDecimal rate = interestRateService.calculate(
                RiskBand.LOW, EmploymentType.SALARIED, new BigDecimal("1000000"));
        // At exactly 10L — not greater than 10L, so no premium
        assertThat(rate).isEqualByComparingTo(new BigDecimal("12.0"));
    }
}
