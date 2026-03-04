package com.rbih.loanservice.service;

import com.rbih.loanservice.domain.enums.RiskBand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Risk Classification Service Tests")
class RiskClassificationServiceTest {

    private RiskClassificationService riskClassificationService;

    @BeforeEach
    void setUp() {
        riskClassificationService = new RiskClassificationService();
    }

    @ParameterizedTest(name = "Credit score {0} should be classified as LOW")
    @CsvSource({"750", "800", "850", "900"})
    @DisplayName("Should classify scores 750+ as LOW risk")
    void shouldClassifyLowRisk(int creditScore) {
        assertThat(riskClassificationService.classify(creditScore)).isEqualTo(RiskBand.LOW);
    }

    @ParameterizedTest(name = "Credit score {0} should be classified as MEDIUM")
    @CsvSource({"650", "700", "749"})
    @DisplayName("Should classify scores 650-749 as MEDIUM risk")
    void shouldClassifyMediumRisk(int creditScore) {
        assertThat(riskClassificationService.classify(creditScore)).isEqualTo(RiskBand.MEDIUM);
    }

    @ParameterizedTest(name = "Credit score {0} should be classified as HIGH")
    @CsvSource({"600", "620", "649"})
    @DisplayName("Should classify scores 600-649 as HIGH risk")
    void shouldClassifyHighRisk(int creditScore) {
        assertThat(riskClassificationService.classify(creditScore)).isEqualTo(RiskBand.HIGH);
    }

    @Test
    @DisplayName("Should throw exception for score below 600")
    void shouldThrowForBelowMinimum() {
        assertThatThrownBy(() -> riskClassificationService.classify(599))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("600");
    }

    @Test
    @DisplayName("Boundary: 749 should be MEDIUM, 750 should be LOW")
    void boundaryBetweenLowAndMedium() {
        assertThat(riskClassificationService.classify(749)).isEqualTo(RiskBand.MEDIUM);
        assertThat(riskClassificationService.classify(750)).isEqualTo(RiskBand.LOW);
    }

    @Test
    @DisplayName("Boundary: 649 should be HIGH, 650 should be MEDIUM")
    void boundaryBetweenMediumAndHigh() {
        assertThat(riskClassificationService.classify(649)).isEqualTo(RiskBand.HIGH);
        assertThat(riskClassificationService.classify(650)).isEqualTo(RiskBand.MEDIUM);
    }
}
