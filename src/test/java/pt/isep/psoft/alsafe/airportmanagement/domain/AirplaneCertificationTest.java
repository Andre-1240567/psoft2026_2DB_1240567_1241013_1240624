package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AirplaneCertificationTest {

    @Test
    void ensureValidCertificationWithExplicitDate() {
        LocalDate date = LocalDate.of(2024, 6, 1);
        AirplaneCertification cert = new AirplaneCertification("A320neo", date);
        assertEquals("A320neo", cert.getModelName());
        assertEquals(date, cert.getCertificationDate());
    }

    @Test
    void ensureValidCertificationWithoutDateDefaultsToToday() {
        AirplaneCertification cert = new AirplaneCertification("A320neo");
        assertEquals(LocalDate.now(), cert.getCertificationDate());
    }

    @Test
    void ensureNullDateDefaultsToToday() {
        AirplaneCertification cert = new AirplaneCertification("A320neo", null);
        assertEquals(LocalDate.now(), cert.getCertificationDate());
    }

    @Test
    void ensureNullModelNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new AirplaneCertification(null, LocalDate.now()));
    }

    @Test
    void ensureBlankModelNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new AirplaneCertification("  ", LocalDate.now()));
    }

    @Test
    void ensureEqualsWorksForSameModelAndDate() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        AirplaneCertification a = new AirplaneCertification("A320neo", date);
        AirplaneCertification b = new AirplaneCertification("A320neo", date);
        assertEquals(a, b);
    }

    @Test
    void ensureEqualsFailsForDifferentModel() {
        LocalDate date = LocalDate.of(2024, 1, 1);
        AirplaneCertification a = new AirplaneCertification("A320neo", date);
        AirplaneCertification b = new AirplaneCertification("B737", date);
        assertNotEquals(a, b);
    }
}