package com.rbih.loanservice.domain.entity;

import com.rbih.loanservice.config.LmsIdGenerator;
import com.rbih.loanservice.domain.enums.ApplicationStatus;
import com.rbih.loanservice.domain.enums.EmploymentType;
import com.rbih.loanservice.domain.enums.LoanPurpose;
import com.rbih.loanservice.domain.enums.RiskBand;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(generator = "lms-id-generator")
    @GenericGenerator(name = "lms-id-generator", type = LmsIdGenerator.class)
    private String  id;

    // Applicant details
    private String applicantName;
    private int applicantAge;
    private BigDecimal monthlyIncome;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    private int creditScore;

    // Loan details
    private BigDecimal loanAmount;
    private int tenureMonths;

    @Enumerated(EnumType.STRING)
    private LoanPurpose loanPurpose;

    // Decision
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    private RiskBand riskBand;

    // Offer details (null if rejected)
    private BigDecimal approvedInterestRate;
    private BigDecimal approvedEmi;
    private BigDecimal totalPayable;

    // Rejection reasons stored as comma-separated string
    @ElementCollection
    @CollectionTable(name = "rejection_reasons", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "reason")
    private List<String> rejectionReasons;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
