package com.rbih.loanservice.domain;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EligibilityResult {

    private final boolean eligible;
    private final List<String> rejectionReasons;

    public static EligibilityResult approved() {
        return EligibilityResult.builder()
                .eligible(true)
                .rejectionReasons(List.of())
                .build();
    }

    public static EligibilityResult rejected(List<String> reasons) {
        return EligibilityResult.builder()
                .eligible(false)
                .rejectionReasons(reasons)
                .build();
    }
}
