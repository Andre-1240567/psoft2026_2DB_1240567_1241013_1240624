package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationTest {

    private GPSCoordinates validCoords() {
        return new GPSCoordinates(38.7, -9.1);
    }

    @Test
    void ensureValidLocationIsCreated() {
        GPSCoordinates coords = validCoords();
        Location loc = new Location("Southern Europe", "Portugal", "Lisbon", coords);
        assertEquals("Southern Europe", loc.getRegion());
        assertEquals("Portugal", loc.getCountry());
        assertEquals("Lisbon", loc.getCity());
        assertEquals(coords, loc.getCoordinates());
    }

    @Test
    void ensureNullRegionThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Location(null, "Portugal", "Lisbon", validCoords()));
    }

    @Test
    void ensureEmptyRegionThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Location("", "Portugal", "Lisbon", validCoords()));
    }

    @Test
    void ensureNullCountryThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Location("Southern Europe", null, "Lisbon", validCoords()));
    }

    @Test
    void ensureEmptyCountryThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Location("Southern Europe", "", "Lisbon", validCoords()));
    }

    @Test
    void ensureNullCityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Location("Southern Europe", "Portugal", null, validCoords()));
    }

    @Test
    void ensureEmptyCityThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Location("Southern Europe", "Portugal", "", validCoords()));
    }

    @Test
    void ensureNullCoordinatesIsAccepted() {
        assertDoesNotThrow(() -> new Location("Southern Europe", "Portugal", "Lisbon", null));
    }
}