package com.rbih.loanservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rbih.loanservice.domain.enums.ApplicationStatus;
import com.rbih.loanservice.domain.enums.RiskBand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LoanApplicationResponse {

    private String applicationId;
    private ApplicationStatus status;
    private RiskBand riskBand;

    // Present only on APPROVED
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private OfferDTO offer;

    // Present only on REJECTED
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> rejectionReasons;
}
