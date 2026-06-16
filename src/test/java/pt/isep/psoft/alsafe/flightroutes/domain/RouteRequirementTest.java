package pt.isep.psoft.alsafe.flightroutes.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteRequirementTest {

    @Test
    void ensureValidRequirementsAreCreated() {
        RouteRequirement req = assertDoesNotThrow(() -> new RouteRequirement(1500.0, 100));

        assertEquals(1500.0, req.getMinRangeRequired());
        assertEquals(100,    req.getMinCapacityRequired());
    }

    @Test
    void ensureMinimumBoundaryValuesAreAccepted() {
        assertDoesNotThrow(() -> new RouteRequirement(0.001, 1));
    }

    @Test
    void ensureZeroRangeIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new RouteRequirement(0.0, 100));
    }

    @Test
    void ensureNegativeRangeIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new RouteRequirement(-1.0, 100));
    }

    @Test
    void ensureZeroCapacityIsRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new RouteRequirement(1500.0, 0));
    }

    @Test
    void ensureNegativeCapacityIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new RouteRequirement(1500.0, -10));

        assertEquals("Minimum capacity required must be a positive value.", ex.getMessage());
    }

    @Test
    void ensureBothZeroRangeAndNegativeCapacityAreRejected() {
        assertThrows(IllegalArgumentException.class, () ->
                new RouteRequirement(0.0, -10));
    }

    @Test
    void ensureNullRangeIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new RouteRequirement(null, 100));

        assertEquals("Minimum range required must be a positive value.", ex.getMessage());
    }

    @Test
    void ensureNullCapacityIsRejected() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new RouteRequirement(1500.0, null));

        assertEquals("Minimum capacity required must be a positive value.", ex.getMessage());
    }
}