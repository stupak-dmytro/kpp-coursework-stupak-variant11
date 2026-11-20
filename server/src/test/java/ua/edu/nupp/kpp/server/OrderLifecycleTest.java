package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderLifecycleTest {

    @ParameterizedTest
    @CsvSource({
            "CREATED, APPROVED",
            "APPROVED, ORDERED",
            "ORDERED, RECEIVED"
    })
    void allowsOnlySequentialForwardTransitions(OrderState from, OrderState to) {
        assertTrue(ReplenishmentOrderService.isAllowedTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "CREATED, ORDERED",
            "CREATED, RECEIVED",
            "APPROVED, RECEIVED"
    })
    void forbidsSkippingStates(OrderState from, OrderState to) {
        assertFalse(ReplenishmentOrderService.isAllowedTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "APPROVED, CREATED",
            "ORDERED, APPROVED",
            "RECEIVED, ORDERED"
    })
    void forbidsBackwardTransitions(OrderState from, OrderState to) {
        assertFalse(ReplenishmentOrderService.isAllowedTransition(from, to));
    }

    @ParameterizedTest
    @CsvSource({
            "CREATED, CREATED",
            "APPROVED, APPROVED",
            "ORDERED, ORDERED",
            "RECEIVED, RECEIVED"
    })
    void forbidsTransitionToTheSameState(OrderState from, OrderState to) {
        assertFalse(ReplenishmentOrderService.isAllowedTransition(from, to));
    }

    @Test
    void receivedIsFinalState() {
        for (OrderState to : OrderState.values()) {
            assertFalse(ReplenishmentOrderService.isAllowedTransition(OrderState.RECEIVED, to));
        }
    }

    @Test
    void exactlyThreeTransitionsAreAllowed() {
        int allowed = 0;
        for (OrderState from : OrderState.values()) {
            for (OrderState to : OrderState.values()) {
                if (ReplenishmentOrderService.isAllowedTransition(from, to)) {
                    allowed++;
                }
            }
        }
        assertEquals(3, allowed);
    }
}
