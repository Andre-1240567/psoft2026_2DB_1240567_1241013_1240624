package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GateTest {

    @Test
    void ensureValidGateIsCreated() {
        Gate g = new Gate("A1");
        assertEquals("A1", g.getDesignation());
    }

    @Test
    void ensureNullDesignationThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Gate(null));
    }

    @Test
    void ensureBlankDesignationThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Gate("   "));
    }

    @Test
    void ensureProtectedConstructorExists() {
        Gate gate = new Gate();
        assertNotNull(gate);
    }
}