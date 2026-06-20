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
    void ensureAircraftUsesModelCapacityIfNoCapacityProvided() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        assertEquals(180, aircraft.getActiveCapacity());
    }

    @Test
    void ensureAircraftUsesProvidedCapacity() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy", 150);
        assertEquals(150, aircraft.getActiveCapacity());
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

    @Test
    void ensureRegistrationNumberCannotBeNullOrEmpty() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft(null, model, LocalDate.now(), "Economy");
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft("   ", model, LocalDate.now(), "Economy");
        });
    }

    @Test
    void ensureModelCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft("CS-TPA", null, LocalDate.now(), "Economy");
        });
    }

    @Test
    void ensureManufacturingDateCannotBeInFuture() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft("CS-TPA", model, LocalDate.now().plusDays(1), "Economy");
        });
    }

    @Test
    void ensureFlightHoursCanBeAdded() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        aircraft.addFlightHours(10.5);
        assertEquals(10.5, aircraft.getTotalFlightHours());
    }

    @Test
    void ensureFlightHoursMustBePositive() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        assertThrows(IllegalArgumentException.class, () -> {
            aircraft.addFlightHours(-5.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            aircraft.addFlightHours(null);
        });
    }

    @Test
    void ensureAssignmentCanBeAdded() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
        assertEquals(0, aircraft.getNumberOfAssignments());
        aircraft.addAssignment();
        assertEquals(1, aircraft.getNumberOfAssignments());
    }

    @Test
    void ensureManufacturingDateCannotBeNull() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        assertThrows(IllegalArgumentException.class, () -> {
            new Aircraft("CS-TPA", model, null, "Economy");
        });
    }

    @Test
    void ensureAircraftUsesModelCapacityIfCapacityIsZeroOrNegative() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        Aircraft aircraft0 = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy", 0);
        assertEquals(180, aircraft0.getActiveCapacity());
        
        Aircraft aircraftNeg = new Aircraft("CS-TPB", model, LocalDate.now(), "Economy", -10);
        assertEquals(180, aircraftNeg.getActiveCapacity());
    }
}