package com.rbih.loanservice.service;

import com.rbih.loanservice.domain.EligibilityResult;
import com.rbih.loanservice.domain.entity.LoanApplication;
import com.rbih.loanservice.domain.enums.ApplicationStatus;
import com.rbih.loanservice.domain.enums.RiskBand;
import com.rbih.loanservice.dto.request.LoanApplicationRequest;
import com.rbih.loanservice.dto.response.LoanApplicationResponse;
import com.rbih.loanservice.dto.response.OfferDTO;
import com.rbih.loanservice.repository.LoanApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the loan application evaluation pipeline:
 * 1. Classify risk band
 * 2. Calculate interest rate
 * 3. Calculate EMI
 * 4. Run eligibility checks
 * 5. Check offer validity (EMI ≤ 50% income)
 * 6. Persist and return result
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private static final BigDecimal OFFER_EMI_LIMIT_PERCENT = new BigDecimal("0.50");

    private final RiskClassificationService riskClassificationService;
    private final InterestRateService interestRateService;
    private final EmiCalculationService emiCalculationService;
    private final EligibilityService eligibilityService;
    private final LoanApplicationRepository repository;

    public LoanApplicationResponse process(LoanApplicationRequest request) {
        var applicant = request.getApplicant();
        var loan = request.getLoan();

        log.info("Processing loan application for applicant: {}", applicant.getName());

        // Step 1: Check basic credit score eligibility before classification
        if (applicant.getCreditScore() < 600) {
            log.info("Application rejected: credit score {} below minimum 600", applicant.getCreditScore());
            return buildAndPersistRejected(request, List.of("CREDIT_SCORE_TOO_LOW"), null);
        }

        // Step 2: Classify risk band
        RiskBand riskBand = riskClassificationService.classify(applicant.getCreditScore());
        log.info("Risk band classified as: {}", riskBand);

        // Step 3: Calculate final interest rate
        BigDecimal interestRate = interestRateService.calculate(
                riskBand,
                applicant.getEmploymentType(),
                loan.getAmount()
        );
        log.info("Final interest rate: {}%", interestRate);

        // Step 4: Calculate EMI using the base 12% rate for eligibility check
        //         (spec says base interest rate = 12% for EMI formula; final rate is used for offer)
        BigDecimal emi = emiCalculationService.calculateEmi(loan.getAmount(), interestRate, loan.getTenureMonths());
        log.info("Calculated EMI: {}", emi);

        // Step 5: Run eligibility rules
        EligibilityResult eligibility = eligibilityService.evaluate(applicant, loan, emi);

        if (!eligibility.isEligible()) {
            log.info("Application rejected for reasons: {}", eligibility.getRejectionReasons());
            return buildAndPersistRejected(request, eligibility.getRejectionReasons(), riskBand);
        }

        // Step 6: Offer generation — EMI must be ≤ 50% of monthly income
        BigDecimal maxOfferEmi = applicant.getMonthlyIncome()
                .multiply(OFFER_EMI_LIMIT_PERCENT)
                .setScale(2, RoundingMode.HALF_UP);

        if (emi.compareTo(maxOfferEmi) > 0) {
            log.info("Offer rejected: EMI {} exceeds 50% of income {}", emi, maxOfferEmi);
            List<String> reasons = new ArrayList<>();
            reasons.add("EMI_EXCEEDS_50_PERCENT");
            return buildAndPersistRejected(request, reasons, riskBand);
        }

        // Step 7: Generate approved offer
        BigDecimal totalPayable = emiCalculationService.calculateTotalPayable(emi, loan.getTenureMonths());

        OfferDTO offer = OfferDTO.builder()
                .interestRate(interestRate)
                .tenureMonths(loan.getTenureMonths())
                .emi(emi)
                .totalPayable(totalPayable)
                .build();

        return buildAndPersistApproved(request, riskBand, interestRate, emi, totalPayable, offer);
    }

    private LoanApplicationResponse buildAndPersistApproved(
            LoanApplicationRequest request,
            RiskBand riskBand,
            BigDecimal interestRate,
            BigDecimal emi,
            BigDecimal totalPayable,
            OfferDTO offer) {

        LoanApplication entity = LoanApplication.builder()
                .applicantName(request.getApplicant().getName())
                .applicantAge(request.getApplicant().getAge())
                .monthlyIncome(request.getApplicant().getMonthlyIncome())
                .employmentType(request.getApplicant().getEmploymentType())
                .creditScore(request.getApplicant().getCreditScore())
                .loanAmount(request.getLoan().getAmount())
                .tenureMonths(request.getLoan().getTenureMonths())
                .loanPurpose(request.getLoan().getPurpose())
                .status(ApplicationStatus.APPROVED)
                .riskBand(riskBand)
                .approvedInterestRate(interestRate)
                .approvedEmi(emi)
                .totalPayable(totalPayable)
                .build();

        LoanApplication saved = repository.save(entity);
        log.info("Approved application persisted with id: {}", saved.getId());

        return LoanApplicationResponse.builder()
                .applicationId(saved.getId())
                .status(ApplicationStatus.APPROVED)
                .riskBand(riskBand)
                .offer(offer)
                .build();
    }

    private LoanApplicationResponse buildAndPersistRejected(
            LoanApplicationRequest request,
            List<String> reasons,
            RiskBand riskBand) {

        LoanApplication entity = LoanApplication.builder()
                .applicantName(request.getApplicant().getName())
                .applicantAge(request.getApplicant().getAge())
                .monthlyIncome(request.getApplicant().getMonthlyIncome())
                .employmentType(request.getApplicant().getEmploymentType())
                .creditScore(request.getApplicant().getCreditScore())
                .loanAmount(request.getLoan().getAmount())
                .tenureMonths(request.getLoan().getTenureMonths())
                .loanPurpose(request.getLoan().getPurpose())
                .status(ApplicationStatus.REJECTED)
                .riskBand(riskBand)
                .rejectionReasons(reasons)
                .build();

        LoanApplication saved = repository.save(entity);
        log.info("Rejected application persisted with id: {}", saved.getId());

        return LoanApplicationResponse.builder()
                .applicationId(saved.getId())
                .status(ApplicationStatus.REJECTED)
                .riskBand(riskBand)
                .rejectionReasons(reasons)
                .build();
    }
}
