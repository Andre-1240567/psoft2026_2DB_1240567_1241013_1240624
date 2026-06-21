package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class OperationalHoursTest {

    @Test
    void ensureValidHoursAreAccepted() {
        OperationalHours oh = new OperationalHours(LocalTime.of(6, 0), LocalTime.of(22, 0));
        assertEquals(LocalTime.of(6, 0), oh.getOpeningTime());
        assertEquals(LocalTime.of(22, 0), oh.getClosingTime());
    }

    @Test
    void ensureNullOpeningTimeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new OperationalHours(null, LocalTime.of(22, 0)));
    }

    @Test
    void ensureNullClosingTimeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new OperationalHours(LocalTime.of(6, 0), null));
    }

    @Test
    void ensureOpeningAfterClosingThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new OperationalHours(LocalTime.of(22, 0), LocalTime.of(6, 0)));
    }

    @Test
    void ensureEqualOpeningAndClosingIsAccepted() {
        assertDoesNotThrow(
                () -> new OperationalHours(LocalTime.of(8, 0), LocalTime.of(8, 0)));
    }
}