package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TerminalTest {

    @Test
    void ensureValidTerminalIsCreated() {
        Terminal t = new Terminal("Terminal 1");
        assertEquals("Terminal 1", t.getDesignation());
        assertTrue(t.getGates().isEmpty());
        assertTrue(t.getServices().isEmpty());
    }

    @Test
    void ensureNullDesignationThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Terminal(null));
    }

    @Test
    void ensureBlankDesignationThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Terminal("  "));
    }

    @Test
    void ensureAddGateSucceeds() {
        Terminal t = new Terminal("T1");
        t.addGate(new Gate("A1"));
        assertEquals(1, t.getGates().size());
        assertEquals("A1", t.getGates().get(0).getDesignation());
    }

    @Test
    void ensureAddNullGateThrows() {
        Terminal t = new Terminal("T1");
        assertThrows(IllegalArgumentException.class, () -> t.addGate(null));
    }

    @Test
    void ensureAddMultipleGatesSucceeds() {
        Terminal t = new Terminal("T1");
        t.addGate(new Gate("A1"));
        t.addGate(new Gate("A2"));
        assertEquals(2, t.getGates().size());
    }

    @Test
    void ensureAddServiceSucceeds() {
        Terminal t = new Terminal("T1");
        t.addService(new FacilityService("WIFI", "Free internet"));
        assertEquals(1, t.getServices().size());
        assertEquals("WIFI", t.getServices().get(0).getServiceType());
    }

    @Test
    void ensureAddNullServiceThrows() {
        Terminal t = new Terminal("T1");
        assertThrows(IllegalArgumentException.class, () -> t.addService(null));
    }

    @Test
    void ensureAddMultipleServicesSucceeds() {
        Terminal t = new Terminal("T1");
        t.addService(new FacilityService("WIFI", "Internet"));
        t.addService(new FacilityService("LOUNGE", "VIP lounge"));
        assertEquals(2, t.getServices().size());
    }
}