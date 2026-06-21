package pt.isep.psoft.alsafe.airportmanagement.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirportTest {


    private IATACode validCode() {
        return new IATACode("LIS");
    }

    private Location validLocation() {
        return new Location("Southern Europe", "Portugal", "Lisbon",
                new GPSCoordinates(38.7749, -9.1342));
    }

    private Timezone validTimezone() {
        return new Timezone("UTC+01:00");
    }

    private Airport validAirport() {
        return new Airport(validCode(), "Humberto Delgado Airport", validLocation(), validTimezone());
    }

    @Test
    void ensureValidAirportIsCreated() {
        Airport airport = validAirport();
        assertEquals("LIS", airport.getIataCode().getCode());
        assertEquals("Humberto Delgado Airport", airport.getName());
        assertEquals(Status.OPERATIONAL, airport.getStatus());
        assertTrue(airport.getRunways().isEmpty());
        assertTrue(airport.getTerminals().isEmpty());
        assertTrue(airport.getCertifications().isEmpty());
        assertTrue(airport.getPhotos().isEmpty());
        assertTrue(airport.getContacts().isEmpty());
    }

    @Test
    void ensureNullIataCodeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Airport(null, "Name", validLocation(), validTimezone()));
    }

    @Test
    void ensureNullNameThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Airport(validCode(), null, validLocation(), validTimezone()));
    }

    @Test
    void ensureNullLocationThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Airport(validCode(), "Name", null, validTimezone()));
    }

    @Test
    void ensureNullTimezoneThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new Airport(validCode(), "Name", validLocation(), null));
    }

    @Test
    void ensureAddRunwaySucceeds() {
        Airport airport = validAirport();
        airport.addRunway(new Runway("28L", 3500.0, Orientation.W));
        assertEquals(1, airport.getRunways().size());
    }

    @Test
    void ensureAddMultipleRunwaysSucceeds() {
        Airport airport = validAirport();
        airport.addRunway(new Runway("28L", 3500.0, Orientation.W));
        airport.addRunway(new Runway("10R", 3200.0, Orientation.E));
        assertEquals(2, airport.getRunways().size());
    }

    @Test
    void ensureAddNullRunwayThrows() {
        Airport airport = validAirport();
        assertThrows(IllegalArgumentException.class, () -> airport.addRunway(null));
    }

    @Test
    void ensureAddTerminalSucceeds() {
        Airport airport = validAirport();
        airport.addTerminal(new Terminal("Terminal 1"));
        assertEquals(1, airport.getTerminals().size());
    }

    @Test
    void ensureAddMultipleTerminalsSucceeds() {
        Airport airport = validAirport();
        airport.addTerminal(new Terminal("Terminal 1"));
        airport.addTerminal(new Terminal("Terminal 2"));
        assertEquals(2, airport.getTerminals().size());
    }

    @Test
    void ensureAddNullTerminalThrows() {
        Airport airport = validAirport();
        assertThrows(IllegalArgumentException.class, () -> airport.addTerminal(null));
    }

    @Test
    void ensureAddPhotoSucceeds() {
        Airport airport = validAirport();
        airport.addPhoto("https://example.com/photo.jpg");
        assertEquals(1, airport.getPhotos().size());
    }

    @Test
    void ensureAddMultiplePhotosSucceeds() {
        Airport airport = validAirport();
        airport.addPhoto("https://example.com/photo1.jpg");
        airport.addPhoto("https://example.com/photo2.jpg");
        assertEquals(2, airport.getPhotos().size());
    }

    @Test
    void ensureAddNullPhotoThrows() {
        Airport airport = validAirport();
        assertThrows(IllegalArgumentException.class, () -> airport.addPhoto(null));
    }

    @Test
    void ensureAddBlankPhotoThrows() {
        Airport airport = validAirport();
        assertThrows(IllegalArgumentException.class, () -> airport.addPhoto("   "));
    }

    @Test
    void ensureStatusChangesToClosed() {
        Airport airport = validAirport();
        airport.changeStatus(Status.CLOSED);
        assertEquals(Status.CLOSED, airport.getStatus());
    }

    @Test
    void ensureStatusChangesToUnderMaintenance() {
        Airport airport = validAirport();
        airport.changeStatus(Status.UNDER_MAINTENANCE);
        assertEquals(Status.UNDER_MAINTENANCE, airport.getStatus());
    }

    @Test
    void ensureNullStatusThrows() {
        Airport airport = validAirport();
        assertThrows(IllegalArgumentException.class, () -> airport.changeStatus(null));
    }

    @Test
    void ensureSameStatusThrows() {
        Airport airport = validAirport();
        assertThrows(IllegalArgumentException.class,
                () -> airport.changeStatus(Status.OPERATIONAL));
    }

    @Test
    void ensureStatusCanChainTransitions() {
        Airport airport = validAirport();
        airport.changeStatus(Status.UNDER_MAINTENANCE);
        airport.changeStatus(Status.OPERATIONAL);
        assertEquals(Status.OPERATIONAL, airport.getStatus());
    }

    @Test
    void ensureAddCertificationSucceeds() {
        Airport airport = validAirport();
        airport.addCertification("A320neo");
        assertEquals(1, airport.getCertifications().size());
        assertEquals("A320neo", airport.getCertifications().get(0).getModelName());
    }

    @Test
    void ensureCertificationDateIsSetToToday() {
        Airport airport = validAirport();
        airport.addCertification("A320neo");
        assertEquals(LocalDate.now(), airport.getCertifications().get(0).getCertificationDate());
    }

    @Test
    void ensureAddDuplicateCertificationThrows() {
        Airport airport = validAirport();
        airport.addCertification("A320neo");
        assertThrows(IllegalArgumentException.class,
                () -> airport.addCertification("A320neo"));
    }

    @Test
    void ensureAddDifferentCertificationsSucceeds() {
        Airport airport = validAirport();
        airport.addCertification("A320neo");
        airport.addCertification("B737");
        assertEquals(2, airport.getCertifications().size());
    }


    @Test
    void ensureUpdateOperationalHoursSucceeds() {
        Airport airport = validAirport();
        OperationalHours oh = new OperationalHours(LocalTime.of(6, 0), LocalTime.of(23, 0));
        airport.updateDetails(oh, null);
        assertEquals(oh, airport.getOperationalHours());
    }

    @Test
    void ensureUpdateContactsSucceeds() {
        Airport airport = validAirport();
        List<Contact> contacts = List.of(new Contact("+351210000000", "Ops", ContactType.PHONE));
        airport.updateDetails(null, contacts);
        assertEquals(1, airport.getContacts().size());
        assertEquals(ContactType.PHONE, airport.getContacts().get(0).getType());
    }

    @Test
    void ensureUpdateBothFieldsSucceeds() {
        Airport airport = validAirport();
        OperationalHours oh = new OperationalHours(LocalTime.of(5, 0), LocalTime.of(22, 0));
        List<Contact> contacts = List.of(new Contact("info@lis.pt", "Info", ContactType.EMAIL));
        airport.updateDetails(oh, contacts);
        assertEquals(oh, airport.getOperationalHours());
        assertEquals(1, airport.getContacts().size());
    }

    @Test
    void ensureUpdateWithBothNullKeepsExistingValues() {
        Airport airport = validAirport();
        OperationalHours oh = new OperationalHours(LocalTime.of(6, 0), LocalTime.of(22, 0));
        airport.updateDetails(oh, null);
        airport.updateDetails(null, null);
        assertEquals(oh, airport.getOperationalHours());
    }

    @Test
    void ensureUpdateContactsReplacesExistingList() {
        Airport airport = validAirport();
        airport.updateDetails(null,
                List.of(new Contact("+351210000000", "Ops", ContactType.PHONE)));
        airport.updateDetails(null,
                List.of(new Contact("info@lis.pt", "Info", ContactType.EMAIL)));
        assertEquals(1, airport.getContacts().size());
        assertEquals(ContactType.EMAIL, airport.getContacts().get(0).getType());
    }
}