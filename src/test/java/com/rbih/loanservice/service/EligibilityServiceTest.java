package com.rbih.loanservice.service;

import com.rbih.loanservice.domain.EligibilityResult;
import com.rbih.loanservice.dto.request.ApplicantDTO;
import com.rbih.loanservice.dto.request.LoanDTO;
import com.rbih.loanservice.domain.enums.EmploymentType;
import com.rbih.loanservice.domain.enums.LoanPurpose;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Eligibility Service Tests")
class EligibilityServiceTest {

    private EligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        eligibilityService = new EligibilityService();
    }

    // ─── Helper builders ──────────────────────────────────────────────────────

    private ApplicantDTO buildApplicant(int age, int creditScore, BigDecimal monthlyIncome) {
        ApplicantDTO applicant = new ApplicantDTO();
        applicant.setName("Test User");
        applicant.setAge(age);
        applicant.setCreditScore(creditScore);
        applicant.setMonthlyIncome(monthlyIncome);
        applicant.setEmploymentType(EmploymentType.SALARIED);
        return applicant;
    }

    private LoanDTO buildLoan(int tenureMonths, BigDecimal amount) {
        LoanDTO loan = new LoanDTO();
        loan.setTenureMonths(tenureMonths);
        loan.setAmount(amount);
        loan.setPurpose(LoanPurpose.PERSONAL);
        return loan;
    }

    // ─── Credit Score Tests ───────────────────────────────────────────────────

    @Test
    @DisplayName("Should reject when credit score is below 600")
    void shouldRejectLowCreditScore() {
        ApplicantDTO applicant = buildApplicant(30, 599, new BigDecimal("75000"));
        LoanDTO loan = buildLoan(36, new BigDecimal("100000"));
        BigDecimal emi = new BigDecimal("5000");

        EligibilityResult result = eligibilityService.evaluate(applicant, loan, emi);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getRejectionReasons()).contains("CREDIT_SCORE_TOO_LOW");
    }

    @Test
    @DisplayName("Should pass when credit score is exactly 600")
    void shouldPassAtBoundaryCreditScore() {
        ApplicantDTO applicant = buildApplicant(30, 600, new BigDecimal("75000"));
        LoanDTO loan = buildLoan(36, new BigDecimal("100000"));
        BigDecimal emi = new BigDecimal("5000");

        EligibilityResult result = eligibilityService.evaluate(applicant, loan, emi);

        assertThat(result.getRejectionReasons()).doesNotContain("CREDIT_SCORE_TOO_LOW");
    }

    // ─── Age + Tenure Tests ────────────────────────────────────────────────────

    @Test
    @DisplayName("Should reject when age + tenure years exceeds 65")
    void shouldRejectWhenAgePlusTenureExceeds65() {
        // age 40 + 26 years tenure = 66 > 65
        ApplicantDTO applicant = buildApplicant(40, 700, new BigDecimal("75000"));
        LoanDTO loan = buildLoan(312, new BigDecimal("100000")); // 312 months = 26 years
        BigDecimal emi = new BigDecimal("5000");

        EligibilityResult result = eligibilityService.evaluate(applicant, loan, emi);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getRejectionReasons()).contains("AGE_TENURE_LIMIT_EXCEEDED");
    }

    @Test
    @DisplayName("Should pass when age + tenure years equals exactly 65")
    void shouldPassWhenAgePlusTenureEquals65() {
        // age 45 + 20 years = 65 (allowed)
        ApplicantDTO applicant = buildApplicant(45, 700, new BigDecimal("75000"));
        LoanDTO loan = buildLoan(240, new BigDecimal("100000")); // 240 months = 20 years
        BigDecimal emi = new BigDecimal("5000");

        EligibilityResult result = eligibilityService.evaluate(applicant, loan, emi);

        assertThat(result.getRejectionReasons()).doesNotContain("AGE_TENURE_LIMIT_EXCEEDED");
    }

    // ─── EMI to Income Tests ──────────────────────────────────────────────────

    @Test
    @DisplayName("Should reject when EMI exceeds 60% of monthly income")
    void shouldRejectWhenEmiExceeds60Percent() {
        ApplicantDTO applicant = buildApplicant(30, 700, new BigDecimal("50000"));
        LoanDTO loan = buildLoan(12, new BigDecimal("500000"));
        BigDecimal emi = new BigDecimal("31000"); // 62% of 50000

        EligibilityResult result = eligibilityService.evaluate(applicant, loan, emi);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getRejectionReasons()).contains("EMI_EXCEEDS_60_PERCENT");
    }

    @Test
    @DisplayName("Should pass when EMI is exactly 60% of monthly income")
    void shouldPassWhenEmiIsExactly60Percent() {
        ApplicantDTO applicant = buildApplicant(30, 700, new BigDecimal("50000"));
        LoanDTO loan = buildLoan(12, new BigDecimal("200000"));
        BigDecimal emi = new BigDecimal("30000"); // exactly 60% of 50000

        EligibilityResult result = eligibilityService.evaluate(applicant, loan, emi);

        assertThat(result.getRejectionReasons()).doesNotContain("EMI_EXCEEDS_60_PERCENT");
    }

    // ─── Combined Rejection Tests ──────────────────────────────────────────────

    @Test
    @DisplayName("Should return all rejection reasons when multiple rules fail")
    void shouldReturnAllRejectionReasons() {
        // credit score < 600, age 58 + 10 years = 68 > 65, high EMI
        ApplicantDTO applicant = buildApplicant(58, 500, new BigDecimal("10000"));
        LoanDTO loan = buildLoan(120, new BigDecimal("500000"));
        BigDecimal emi = new BigDecimal("9000"); // 90% of income

        EligibilityResult result = eligibilityService.evaluate(applicant, loan, emi);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getRejectionReasons())
                .contains("CREDIT_SCORE_TOO_LOW", "AGE_TENURE_LIMIT_EXCEEDED", "EMI_EXCEEDS_60_PERCENT");
    }

    @Test
    @DisplayName("Should approve when all eligibility rules pass")
    void shouldApproveValidApplication() {
        ApplicantDTO applicant = buildApplicant(30, 720, new BigDecimal("75000"));
        LoanDTO loan = buildLoan(36, new BigDecimal("500000"));
        BigDecimal emi = new BigDecimal("16000"); // ~21% of income

        EligibilityResult result = eligibilityService.evaluate(applicant, loan, emi);

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getRejectionReasons()).isEmpty();
    }
}
