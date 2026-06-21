package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IATACodeTest {

    @Test
    void ensureValidCodeIsAccepted() {
        IATACode code = new IATACode("LIS");
        assertEquals("LIS", code.getCode());
    }

    @Test
    void ensureNullCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new IATACode(null));
    }

    @Test
    void ensureCodeWithLowercaseThrows() {
        assertThrows(IllegalArgumentException.class, () -> new IATACode("lis"));
    }

    @Test
    void ensureCodeWithTwoLettersThrows() {
        assertThrows(IllegalArgumentException.class, () -> new IATACode("LI"));
    }

    @Test
    void ensureCodeWithFourLettersThrows() {
        assertThrows(IllegalArgumentException.class, () -> new IATACode("LISB"));
    }

    @Test
    void ensureCodeWithDigitsThrows() {
        assertThrows(IllegalArgumentException.class, () -> new IATACode("L1S"));
    }

    @Test
    void ensureEmptyCodeThrows() {
        assertThrows(IllegalArgumentException.class, () -> new IATACode(""));
    }
}