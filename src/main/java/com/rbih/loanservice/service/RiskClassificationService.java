package com.rbih.loanservice.service;

import com.rbih.loanservice.domain.enums.RiskBand;
import org.springframework.stereotype.Service;

/**
 * Classifies applicant risk based on credit score as per assignment spec:
 * 750+      → LOW
 * 650–749   → MEDIUM
 * 600–649   → HIGH
 * < 600     → ineligible (handled in EligibilityService)
 */
@Service
public class RiskClassificationService {

    public RiskBand classify(int creditScore) {
        if (creditScore >= 750) {
            return RiskBand.LOW;
        } else if (creditScore >= 650) {
            return RiskBand.MEDIUM;
        } else if (creditScore >= 600) {
            return RiskBand.HIGH;
        }
        // Below 600 is ineligible 
        throw new IllegalArgumentException("Credit score " + creditScore + " is below minimum eligibility threshold of 600");
    }
}
