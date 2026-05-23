package pt.isep.psoft.alsafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AircraftModelTest {

    @Test
    void ensureAircraftModelIsCreatedWithValidData() {
        //Happy path
        assertDoesNotThrow(() -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        });
    }

    @Test
    void ensureManufacturerCannotBeNull() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(null, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        });
        assertEquals("Manufacturer cannot be null.", exception.getMessage());
    }

    @Test
    void ensureModelNameCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "   ", 180, 26000.0, 6500.0, 840.0);
        });
    }

    @Test
    void ensureSeatingCapacityMustBeStrictlyPositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 0, 26000.0, 6500.0, 840.0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", -50, 26000.0, 6500.0, 840.0);
        });
    }

    @Test
    void ensureFuelCapacityMustBeStrictlyPositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 0.0, 6500.0, 840.0);
        });
    }
}
