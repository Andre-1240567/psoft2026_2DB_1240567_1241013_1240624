package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GPSCoordinatesTest {

    @Test
    void ensureValidCoordinatesAreAccepted() {
        GPSCoordinates coords = new GPSCoordinates(38.7, -9.1);
        assertEquals(38.7, coords.getLatitude());
        assertEquals(-9.1, coords.getLongitude());
    }

    @Test
    void ensureBoundaryLatitudeMinusNinetyIsAccepted() {
        assertDoesNotThrow(() -> new GPSCoordinates(-90.0, 0.0));
    }

    @Test
    void ensureBoundaryLatitudePlusNinetyIsAccepted() {
        assertDoesNotThrow(() -> new GPSCoordinates(90.0, 0.0));
    }

    @Test
    void ensureLatitudeBelowMinusNinetyThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GPSCoordinates(-90.1, 0.0));
    }

    @Test
    void ensureLatitudeAbovePlusNinetyThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GPSCoordinates(90.1, 0.0));
    }

    @Test
    void ensureNullLatitudeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GPSCoordinates(null, 0.0));
    }

    @Test
    void ensureBoundaryLongitudeMinusOneEightyIsAccepted() {
        assertDoesNotThrow(() -> new GPSCoordinates(0.0, -180.0));
    }

    @Test
    void ensureBoundaryLongitudePlusOneEightyIsAccepted() {
        assertDoesNotThrow(() -> new GPSCoordinates(0.0, 180.0));
    }

    @Test
    void ensureLongitudeBelowMinusOneEightyThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GPSCoordinates(0.0, -180.1));
    }

    @Test
    void ensureLongitudeAbovePlusOneEightyThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GPSCoordinates(0.0, 180.1));
    }

    @Test
    void ensureNullLongitudeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new GPSCoordinates(0.0, null));
    }
}