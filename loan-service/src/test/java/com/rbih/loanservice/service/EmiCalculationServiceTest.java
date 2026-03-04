package com.rbih.loanservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EMI Calculation Service Tests")
class EmiCalculationServiceTest {

    private EmiCalculationService emiCalculationService;

    @BeforeEach
    void setUp() {
        emiCalculationService = new EmiCalculationService();
    }

    @Test
    @DisplayName("Should calculate correct EMI for standard loan")
    void shouldCalculateCorrectEmi() {
        // Given: 500,000 principal, 12% annual rate, 36 months
        BigDecimal principal = new BigDecimal("500000");
        BigDecimal annualRate = new BigDecimal("12.0");
        int tenure = 36;

        // When
        BigDecimal emi = emiCalculationService.calculateEmi(principal, annualRate, tenure);

        // Then: Expected ~16,607.15 based on standard EMI formula
        assertThat(emi).isEqualByComparingTo(new BigDecimal("16607.15"));
    }

    @Test
    @DisplayName("Should calculate correct EMI with 13.5% interest rate")
    void shouldCalculateEmiWithHigherRate() {
        BigDecimal principal = new BigDecimal("500000");
        BigDecimal annualRate = new BigDecimal("13.5");
        int tenure = 36;

        BigDecimal emi = emiCalculationService.calculateEmi(principal, annualRate, tenure);

        // Should be higher than at 12%
        assertThat(emi).isGreaterThan(new BigDecimal("16607.15"));
    }

    @Test
    @DisplayName("Should calculate total payable correctly")
    void shouldCalculateTotalPayable() {
        BigDecimal emi = new BigDecimal("16607.15");
        int tenure = 36;

        BigDecimal totalPayable = emiCalculationService.calculateTotalPayable(emi, tenure);

        assertThat(totalPayable).isEqualByComparingTo(new BigDecimal("597857.40"));
    }

    @Test
    @DisplayName("EMI should scale with principal")
    void emiShouldScaleWithPrincipal() {
        BigDecimal annualRate = new BigDecimal("12.0");
        int tenure = 12;

        BigDecimal emi100k = emiCalculationService.calculateEmi(new BigDecimal("100000"), annualRate, tenure);
        BigDecimal emi200k = emiCalculationService.calculateEmi(new BigDecimal("200000"), annualRate, tenure);

        // Double principal should give double EMI
        assertThat(emi200k).isEqualByComparingTo(emi100k.multiply(new BigDecimal("2")));
    }

    @Test
    @DisplayName("EMI should decrease as tenure increases")
    void emiShouldDecreaseAstenureIncreases() {
        BigDecimal principal = new BigDecimal("300000");
        BigDecimal annualRate = new BigDecimal("12.0");

        BigDecimal emi12 = emiCalculationService.calculateEmi(principal, annualRate, 12);
        BigDecimal emi36 = emiCalculationService.calculateEmi(principal, annualRate, 36);
        BigDecimal emi60 = emiCalculationService.calculateEmi(principal, annualRate, 60);

        assertThat(emi12).isGreaterThan(emi36);
        assertThat(emi36).isGreaterThan(emi60);
    }
}
