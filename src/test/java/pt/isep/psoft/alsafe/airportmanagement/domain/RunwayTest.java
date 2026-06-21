package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunwayTest {

    @Test
    void ensureValidRunwayIsCreated() {
        Runway r = new Runway("28L", 3500.0, Orientation.W);
        assertEquals("28L", r.getName());
        assertEquals(3500.0, r.getLength());
        assertEquals(Orientation.W, r.getOrientation());
    }

    @Test
    void ensureNullOrientationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Runway("28L", 3500.0, null));
    }

    @Test
    void ensureNullNameIsAccepted() {
        assertDoesNotThrow(() -> new Runway(null, 3500.0, Orientation.N));
    }

    @Test
    void ensureNullLengthIsAccepted() {
        assertDoesNotThrow(() -> new Runway("28L", null, Orientation.N));
    }

    @Test
    void ensureProtectedConstructorExists() {
        Runway runway = new Runway();
        assertNotNull(runway);
    }
}