package pt.isep.psoft.alsafe.flightroutes.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RouteRequirementTest {

    @Test
    void ensureMustHavePositiveRangeAndCapacity() {
        // Arrange - Nothing to Arrange in this particular case
        
        // Act & Assert 
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new RouteRequirement(0.0, -10);
        });

        assertEquals("O alcance e a capacidade devem ser maiores que zero.", exception.getMessage());
    }

    @Test
    void ensureValidRequirementsAreCreated() {
        assertDoesNotThrow(() -> {
            new RouteRequirement(1500.0, 100);
        });
    }
}