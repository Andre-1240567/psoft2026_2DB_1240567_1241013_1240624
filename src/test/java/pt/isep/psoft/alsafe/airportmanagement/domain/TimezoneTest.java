package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TimezoneTest {

    @Test
    void ensureValidPositiveOffsetIsAccepted() {
        Timezone tz = new Timezone("UTC+01:00");
        assertEquals("UTC+01:00", tz.getOffsetValue());
    }

    @Test
    void ensureValidNegativeOffsetIsAccepted() {
        assertDoesNotThrow(() -> new Timezone("UTC-05:00"));
    }

    @Test
    void ensureMaxPositiveOffsetIsAccepted() {
        assertDoesNotThrow(() -> new Timezone("UTC+14:00"));
    }

    @Test
    void ensureZeroOffsetIsAccepted() {
        assertDoesNotThrow(() -> new Timezone("UTC+00:00"));
    }

    @Test
    void ensureNullOffsetThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Timezone(null));
    }

    @Test
    void ensureOffsetWithoutUTCPrefixThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Timezone("+01:00"));
    }

    @Test
    void ensureOffsetWithHourAbove14Throws() {
        assertThrows(IllegalArgumentException.class, () -> new Timezone("UTC+15:00"));
    }

    @Test
    void ensureOffsetWithInvalidMinutesThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Timezone("UTC+01:60"));
    }

    @Test
    void ensureOffsetWithoutColonThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Timezone("UTC+0100"));
    }

    @Test
    void ensureEmptyOffsetThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Timezone(""));
    }
}