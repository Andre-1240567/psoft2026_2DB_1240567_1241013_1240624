package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FacilityServiceTest {

    @Test
    void ensureValidServiceIsCreated() {
        FacilityService fs = new FacilityService("WIFI", "Free wireless internet");
        assertEquals("WIFI", fs.getServiceType());
        assertEquals("Free wireless internet", fs.getDescription());
    }

    @Test
    void ensureNullServiceTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new FacilityService(null, "desc"));
    }

    @Test
    void ensureBlankServiceTypeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new FacilityService("  ", "desc"));
    }

    @Test
    void ensureNullDescriptionIsAccepted() {
        assertDoesNotThrow(() -> new FacilityService("WIFI", null));
    }
}