package pt.isep.psoft.alsafe.aircraftmanagement.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class AircraftTest {

    @Test
    void ensureAircraftCanBeCreatedWithValidData() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        assertDoesNotThrow(() -> {
            new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        });
    }

    @Test
    void ensureAircraftStatusCanBeUpdated() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        aircraft.updateStatus(AircraftStatus.UNDER_MAINTENANCE);
        assertEquals(AircraftStatus.UNDER_MAINTENANCE, aircraft.getStatus());
    }

    @Test
    void ensureStatusCannotBeNullWhenUpdating() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        assertThrows(IllegalArgumentException.class, () -> {
            aircraft.updateStatus(null);
        });
    }
}