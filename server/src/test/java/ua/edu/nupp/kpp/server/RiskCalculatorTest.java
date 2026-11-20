package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiskCalculatorTest {

    private static final double DELTA = 0.001;

    @Test
    void factor1ReturnsRelativeDeficitInPercent() {
        assertEquals(20.0, RiskCalculator.factor1(120, 150), DELTA);
    }

    @Test
    void factor1IsZeroWhenStockIsNotBelowMinimum() {
        assertEquals(0.0, RiskCalculator.factor1(150, 150), DELTA);
        assertEquals(0.0, RiskCalculator.factor1(200, 150), DELTA);
    }

    @Test
    void factor1Is100WhenStockIsEmpty() {
        assertEquals(100.0, RiskCalculator.factor1(0, 150), DELTA);
    }

    @Test
    void factor1RejectsNonPositiveMinLevel() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor1(10, 0));
    }

    @Test
    void factor2NormalizesConsumptionByMinLevel() {
        assertEquals(26.67, RiskCalculator.factor2(40, 150), DELTA);
    }

    @Test
    void factor2IsLimitedTo100() {
        assertEquals(100.0, RiskCalculator.factor2(300, 150), DELTA);
    }

    @Test
    void factor3CombinesReliabilityAndLeadTime() {
        assertEquals(18.21, RiskCalculator.factor3(3, 85), DELTA);
        assertEquals(45.0, RiskCalculator.factor3(7, 60), DELTA);
    }

    @Test
    void factor3HasExpectedBounds() {
        assertEquals(0.0, RiskCalculator.factor3(0, 100), DELTA);
        assertEquals(50.0, RiskCalculator.factor3(14, 100), DELTA);
        assertEquals(100.0, RiskCalculator.factor3(30, 0), DELTA);
    }

    @Test
    void factor3RejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor3(7, 120));
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.factor3(-1, 50));
    }

    @Test
    void calculateUsesWeights040035025() {
        assertEquals(44.25, RiskCalculator.calculate(65, 20, 45), DELTA);
        assertEquals(59.3, RiskCalculator.calculate(52, 60, 70), DELTA);
    }

    @Test
    void calculateKeepsResultInRange() {
        assertEquals(0.0, RiskCalculator.calculate(0, 0, 0), DELTA);
        assertEquals(100.0, RiskCalculator.calculate(100, 100, 100), DELTA);
    }

    @Test
    void calculateRejectsFactorsOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.calculate(101, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> RiskCalculator.calculate(0, -1, 0));
    }
}
