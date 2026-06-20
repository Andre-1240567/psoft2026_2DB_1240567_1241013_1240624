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

    @Test
    void ensureUpdateSpecificationsChangesValues() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        model.updateSpecifications(200, 27000.0, 6600.0, 850.0);
        
        assertEquals(200, model.getSeatingCapacity());
        assertEquals(27000.0, model.getFuelCapacity());
        assertEquals(6600.0, model.getMaxRange());
        assertEquals(850.0, model.getCruisingSpeed());
    }

    @Test
    void ensureUpdateSpecificationsValidatesStrictlyPositive() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            model.updateSpecifications(0, 27000.0, 6600.0, 850.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            model.updateSpecifications(200, -10.0, 6600.0, 850.0);
        });
    }
    @Test
    void ensureAircraftModelIsCreatedWithImage() {
        String img = "path/to/image.png";
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0, img);
        assertEquals(img, model.getImage());
    }

    @Test
    void ensureMaxRangeMustBeStrictlyPositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 0.0, 840.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, -100.0, 840.0);
        });
    }

    @Test
    void ensureCruisingSpeedMustBeStrictlyPositive() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 0.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, -50.0);
        });
    }

    @Test
    void ensureUpdateSpecificationsValidatesMaxRangeAndCruisingSpeed() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            model.updateSpecifications(200, 27000.0, 0.0, 850.0);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            model.updateSpecifications(200, 27000.0, 6600.0, 0.0);
        });
    }

    @Test
    void ensureModelNameCannotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, null, 180, 26000.0, 6500.0, 840.0);
        });
    }

    @Test
    void ensureNullParametersThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", null, 26000.0, 6500.0, 840.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, null, 6500.0, 840.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, null, 840.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, null);
        });
    }

    @Test
    void ensureUpdateSpecificationsValidatesNullParameters() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        assertThrows(IllegalArgumentException.class, () -> {
            model.updateSpecifications(null, 27000.0, 6600.0, 850.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            model.updateSpecifications(200, null, 6600.0, 850.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            model.updateSpecifications(200, 27000.0, null, 850.0);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            model.updateSpecifications(200, 27000.0, 6600.0, null);
        });
    }
}
