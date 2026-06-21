package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirportViewDTOTest {

    @Test
    void ensureDTOIsMappedCorrectlyWithAllData() {
        IATACode iata = new IATACode("OPO");
        GPSCoordinates gps = new GPSCoordinates(41.2356, -8.6780);
        Location location = new Location("Europe", "Portugal", "Porto", gps);
        Timezone tz = new Timezone("UTC+01:00");
        
        Airport airport = new Airport(iata, "Francisco Sa Carneiro", location, tz);
        
        airport.addCertification("737 MAX");
        airport.addPhoto("http://example.com/photo1.jpg");

        OperationalHours opHours = new OperationalHours(LocalTime.of(6, 0), LocalTime.of(23, 30));
        Contact contact = new Contact("info@opo.pt", "Geral", ContactType.EMAIL);
        airport.updateDetails(opHours, List.of(contact));

        Terminal terminal = new Terminal("T1");
        terminal.addGate(new Gate("A1"));
        terminal.addService(new FacilityService("WiFi", "Free Airport WiFi"));
        airport.addTerminal(terminal);

        AirportViewDTO dto = new AirportViewDTO(airport);

        assertEquals("OPO", dto.getIataCode());
        assertEquals("Francisco Sa Carneiro", dto.getName());
        assertEquals("Europe", dto.getRegion());
        assertEquals("Portugal", dto.getCountry());
        assertEquals("Porto", dto.getCity());
        assertEquals("OPERATIONAL", dto.getStatus());
        
        assertEquals(1, dto.getPhotos().size());
        assertEquals("http://example.com/photo1.jpg", dto.getPhotos().get(0));
        assertEquals(1, dto.getCertifications().size());
        assertEquals("737 MAX", dto.getCertifications().get(0));

        assertNotNull(dto.getOperationalHours());
        assertEquals("06:00", dto.getOperationalHours().getOpeningTime());
        assertEquals("23:30", dto.getOperationalHours().getClosingTime());

        assertEquals(1, dto.getContacts().size());
        assertEquals("info@opo.pt", dto.getContacts().get(0).getValue());

        assertNotNull(dto.getTerminals());
        assertEquals(1, dto.getTerminals().size());
        
        AirportViewDTO.TerminalViewDTO terminalDTO = dto.getTerminals().get(0);
        assertEquals("T1", terminalDTO.getDesignation());
        
        assertEquals(1, terminalDTO.getGates().size());
        assertEquals("A1", terminalDTO.getGates().get(0));
        
        assertEquals(1, terminalDTO.getServices().size());
        assertEquals("WiFi", terminalDTO.getServices().get(0).getServiceType());
        assertEquals("Free Airport WiFi", terminalDTO.getServices().get(0).getDescription());
    }

    @Test
    void ensureDTOIsMappedCorrectlyWithMissingOptionalData() {
        IATACode iata = new IATACode("LIS");
        GPSCoordinates gps = new GPSCoordinates(38.7742, -9.1342);
        Location location = new Location("Europe", "Portugal", "Lisbon", gps);
        Timezone tz = new Timezone("UTC+00:00");
        
        Airport airport = new Airport(iata, "Humberto Delgado", location, tz);

        AirportViewDTO dto = new AirportViewDTO(airport);

        assertEquals("LIS", dto.getIataCode());
        assertNull(dto.getOperationalHours(), "Operational hours should be null");
        assertTrue(dto.getTerminals().isEmpty(), "Terminals list should be empty");
        assertTrue(dto.getContacts().isEmpty(), "Contacts list should be empty");
        assertTrue(dto.getPhotos().isEmpty(), "Photos list should be empty");
    }
}