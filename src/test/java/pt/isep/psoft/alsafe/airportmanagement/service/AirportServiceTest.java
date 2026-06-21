package pt.isep.psoft.alsafe.airportmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.*;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportServiceTest {

    @Mock
    private AirportRepository airportRepository;

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private AirportService airportService;

    private Airport dummyAirport;

    @BeforeEach
    void setUp() {
        GPSCoordinates coordinates = new GPSCoordinates(33.9425, -118.4072);
        Location location = new Location("North America", "USA", "Los Angeles", coordinates);
        IATACode iataCode = new IATACode("LAX");
        Timezone timezone = new Timezone("UTC-07:00");
        dummyAirport = new Airport(iataCode, "Los Angeles International Airport", location, timezone);
    }

    @Test
    void ensureCreateAirportSuccess() {
        CreateAirportRequestDTO dto = mock(CreateAirportRequestDTO.class);
        when(dto.getIataCode()).thenReturn("JFK");
        when(dto.getLatitude()).thenReturn(40.6413);
        when(dto.getLongitude()).thenReturn(-73.7781);
        when(dto.getRegion()).thenReturn("North America");
        when(dto.getCountry()).thenReturn("USA");
        when(dto.getCity()).thenReturn("New York");
        when(dto.getTimezone()).thenReturn("UTC-04:00");
        when(dto.getName()).thenReturn("John F. Kennedy International");

        when(airportRepository.findByIataCode_Code("JFK")).thenReturn(Optional.empty());
        when(airportRepository.save(any(Airport.class))).thenReturn(dummyAirport);

        Airport result = airportService.createAirport(dto);

        assertNotNull(result);
        verify(airportRepository).save(any(Airport.class));
    }

    @Test
    void ensureCreateAirportFailsIfAlreadyExists() {
        CreateAirportRequestDTO dto = mock(CreateAirportRequestDTO.class);
        when(dto.getIataCode()).thenReturn("LAX");
        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> airportService.createAirport(dto));

        assertTrue(ex.getMessage().contains("already exists"));
        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureCreateAirportWithoutGPSCoordinatesSuccess() {
        CreateAirportRequestDTO dto = mock(CreateAirportRequestDTO.class);
        
        when(dto.getIataCode()).thenReturn("CDG");
        
        when(dto.getLatitude()).thenReturn(null);
        
        org.mockito.Mockito.lenient().when(dto.getLongitude()).thenReturn(null);
        
        when(dto.getRegion()).thenReturn("Europe");
        when(dto.getCountry()).thenReturn("France");
        when(dto.getCity()).thenReturn("Paris");
        when(dto.getTimezone()).thenReturn("UTC+01:00");
        when(dto.getName()).thenReturn("Charles de Gaulle");

        when(airportRepository.findByIataCode_Code("CDG")).thenReturn(Optional.empty());
        when(airportRepository.save(any(Airport.class))).thenReturn(dummyAirport);

        Airport result = airportService.createAirport(dto);

        assertNotNull(result);
        verify(airportRepository).save(any(Airport.class));
    }

    @Test
    void ensureCreateAirportMapsRunwaysCorrectly() {
        CreateRunwayRequestDTO runwayDTO = new CreateRunwayRequestDTO();
        runwayDTO.setName("09L");
        runwayDTO.setLength(3500.0);
        runwayDTO.setOrientation(Orientation.E);

        CreateAirportRequestDTO dto = mock(CreateAirportRequestDTO.class);
        when(dto.getIataCode()).thenReturn("FRA");
        when(dto.getLatitude()).thenReturn(50.0379);
        when(dto.getLongitude()).thenReturn(8.5622);
        when(dto.getRegion()).thenReturn("Europe");
        when(dto.getCountry()).thenReturn("Germany");
        when(dto.getCity()).thenReturn("Frankfurt");
        when(dto.getTimezone()).thenReturn("UTC+01:00");
        when(dto.getName()).thenReturn("Frankfurt Airport");
        when(dto.getRunways()).thenReturn(List.of(runwayDTO));

        when(airportRepository.findByIataCode_Code("FRA")).thenReturn(Optional.empty());
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        Airport result = airportService.createAirport(dto);

        assertNotNull(result);
        assertEquals(1, result.getRunways().size());
        assertEquals("09L", result.getRunways().get(0).getName());
    }

    @Test
    void ensureCreateAirportMapsPhotosCorrectly() {
        CreateAirportRequestDTO dto = mock(CreateAirportRequestDTO.class);
        when(dto.getIataCode()).thenReturn("MAD");
        when(dto.getLatitude()).thenReturn(40.4719);
        when(dto.getLongitude()).thenReturn(-3.5626);
        when(dto.getRegion()).thenReturn("Europe");
        when(dto.getCountry()).thenReturn("Spain");
        when(dto.getCity()).thenReturn("Madrid");
        when(dto.getTimezone()).thenReturn("UTC+01:00");
        when(dto.getName()).thenReturn("Adolfo Suarez Madrid-Barajas");
        when(dto.getPhotos()).thenReturn(List.of("https://img.example.com/mad1.jpg",
                "https://img.example.com/mad2.jpg"));

        when(airportRepository.findByIataCode_Code("MAD")).thenReturn(Optional.empty());
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        Airport result = airportService.createAirport(dto);

        assertNotNull(result);
        assertEquals(2, result.getPhotos().size());
    }

    @Test
    void ensureCreateAirportMapsTerminalsWithGatesAndServicesCorrectly() {
        CreateServiceDTO serviceDTO = new CreateServiceDTO();
        serviceDTO.setServiceType("WiFi");
        serviceDTO.setDescription("Free Wi-Fi");

        CreateTerminalRequestDTO terminalDTO = new CreateTerminalRequestDTO();
        terminalDTO.setDesignation("T1");
        terminalDTO.setGates(List.of("A1", "A2"));
        terminalDTO.setServices(List.of(serviceDTO));

        CreateAirportRequestDTO dto = mock(CreateAirportRequestDTO.class);
        when(dto.getIataCode()).thenReturn("LHR");
        when(dto.getLatitude()).thenReturn(51.4700);
        when(dto.getLongitude()).thenReturn(-0.4543);
        when(dto.getRegion()).thenReturn("Europe");
        when(dto.getCountry()).thenReturn("UK");
        when(dto.getCity()).thenReturn("London");
        when(dto.getTimezone()).thenReturn("UTC+01:00");
        when(dto.getName()).thenReturn("Heathrow Airport");
        when(dto.getTerminals()).thenReturn(List.of(terminalDTO));

        when(airportRepository.findByIataCode_Code("LHR")).thenReturn(Optional.empty());
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        Airport result = airportService.createAirport(dto);

        assertNotNull(result);
        assertEquals(1, result.getTerminals().size());
        assertEquals("T1", result.getTerminals().get(0).getDesignation());
        assertEquals(2, result.getTerminals().get(0).getGates().size());
        assertEquals(1, result.getTerminals().get(0).getServices().size());
    }

    @Test
    void ensureGetAirportDetailsSuccess() {
        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));

        Airport result = airportService.getAirportDetails("LAX");

        assertNotNull(result);
        assertEquals(dummyAirport, result);
        verify(airportRepository).findByIataCode_Code("LAX");
    }

    @Test
    void ensureGetAirportDetailsFailsIfNotFound() {
        when(airportRepository.findByIataCode_Code("ZZZ")).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> airportService.getAirportDetails("ZZZ"));

        assertTrue(ex.getMessage().contains("not found"));
        verify(airportRepository).findByIataCode_Code("ZZZ");
    }

    @Test
    void ensureChangeOperationalStatusSuccess() {
        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));
        when(airportRepository.save(any(Airport.class))).thenReturn(dummyAirport);

        Airport result = airportService.changeOperationalStatus("LAX", "UNDER_MAINTENANCE");

        assertNotNull(result);
        verify(airportRepository).save(dummyAirport);
    }

    @Test
    void ensureChangeOperationalStatusFailsForInvalidStatus() {
        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> airportService.changeOperationalStatus("LAX", "FLYING"));

        assertTrue(ex.getMessage().contains("Invalid status"));
        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureChangeOperationalStatusFailsIfAirportNotFound() {
        when(airportRepository.findByIataCode_Code("ZZZ")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> airportService.changeOperationalStatus("ZZZ", "CLOSED"));

        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureChangeOperationalStatusFailsIfAlreadyInThatStatus() {
        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));

        assertThrows(IllegalArgumentException.class,
                () -> airportService.changeOperationalStatus("LAX", "OPERATIONAL"));

        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureSearchAirportsSuccess() {
        when(airportRepository.searchAirports("Los Angeles", null, null))
                .thenReturn(List.of(dummyAirport));

        List<Airport> result = airportService.searchAirports("Los Angeles", null, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dummyAirport, result.get(0));
        verify(airportRepository).searchAirports("Los Angeles", null, null);
    }

    @Test
    void ensureSearchAirportsReturnsEmptyList() {
        when(airportRepository.searchAirports("Nowhere", null, null)).thenReturn(new ArrayList<>());

        List<Airport> result = airportService.searchAirports("Nowhere", null, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureAddAirplaneCertificationSuccess() {
        String modelName = "737 MAX";

        when(aircraftModelRepository.findByModelName(modelName))
                .thenReturn(Optional.of(mock(AircraftModel.class)));
        when(airportRepository.findByIataCode_Code("LAX"))
                .thenReturn(Optional.of(dummyAirport));
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        Airport result = airportService.addAirplaneCertification("LAX", modelName);

        assertNotNull(result);
        assertEquals(1, result.getCertifications().size());
        assertEquals(modelName, result.getCertifications().get(0).getModelName());
        verify(airportRepository).save(dummyAirport);
    }

    @Test
    void ensureAddAirplaneCertificationFailsIfModelDoesNotExist() {
        when(aircraftModelRepository.findByModelName("GHOST-PLANE")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> airportService.addAirplaneCertification("LAX", "GHOST-PLANE"));

        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureAddAirplaneCertificationFailsIfAirportNotFound() {
        when(aircraftModelRepository.findByModelName("737 MAX"))
                .thenReturn(Optional.of(mock(AircraftModel.class)));
        when(airportRepository.findByIataCode_Code("ZZZ")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> airportService.addAirplaneCertification("ZZZ", "737 MAX"));

        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureAddAirplaneCertificationFailsIfAlreadyCertified() {
        when(aircraftModelRepository.findByModelName("737 MAX"))
                .thenReturn(Optional.of(mock(AircraftModel.class)));
        when(airportRepository.findByIataCode_Code("LAX"))
                .thenReturn(Optional.of(dummyAirport));
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        airportService.addAirplaneCertification("LAX", "737 MAX");

        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));

        assertThrows(IllegalArgumentException.class,
                () -> airportService.addAirplaneCertification("LAX", "737 MAX"));
    }

    @Test
    void ensureUpdateAirportDetailsWithBothFieldsSuccess() {
        UpdateAirportDetailsRequestDTO dto = mock(UpdateAirportDetailsRequestDTO.class);

        OperationalHoursDTO opHoursDTO = mock(OperationalHoursDTO.class);
        when(opHoursDTO.getOpeningTime()).thenReturn("08:00");
        when(opHoursDTO.getClosingTime()).thenReturn("22:00");
        when(dto.getOperationalHours()).thenReturn(opHoursDTO);

        ContactDTO contactDTO = mock(ContactDTO.class);
        when(contactDTO.getValue()).thenReturn("+351221234567");
        when(contactDTO.getDepartment()).thenReturn("Administration");
        when(contactDTO.getType()).thenReturn(ContactType.PHONE);
        when(dto.getContacts()).thenReturn(List.of(contactDTO));

        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));
        when(airportRepository.save(any(Airport.class))).thenReturn(dummyAirport);

        Airport result = airportService.updateAirportDetails("LAX", dto);

        assertNotNull(result);
        verify(airportRepository).findByIataCode_Code("LAX");
        verify(airportRepository).save(dummyAirport);
    }

    @Test
    void ensureUpdateAirportDetailsWithOnlyOperationalHoursSuccess() {
        UpdateAirportDetailsRequestDTO dto = mock(UpdateAirportDetailsRequestDTO.class);

        OperationalHoursDTO opHoursDTO = mock(OperationalHoursDTO.class);
        when(opHoursDTO.getOpeningTime()).thenReturn("06:00");
        when(opHoursDTO.getClosingTime()).thenReturn("23:00");
        when(dto.getOperationalHours()).thenReturn(opHoursDTO);
        when(dto.getContacts()).thenReturn(null);

        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        Airport result = airportService.updateAirportDetails("LAX", dto);

        assertNotNull(result);
        assertNotNull(result.getOperationalHours());
        assertTrue(result.getContacts().isEmpty());
    }

    @Test
    void ensureUpdateAirportDetailsWithOnlyContactsSuccess() {
        UpdateAirportDetailsRequestDTO dto = mock(UpdateAirportDetailsRequestDTO.class);
        when(dto.getOperationalHours()).thenReturn(null);

        ContactDTO contactDTO = mock(ContactDTO.class);
        when(contactDTO.getValue()).thenReturn("info@lax.com");
        when(contactDTO.getDepartment()).thenReturn("Info");
        when(contactDTO.getType()).thenReturn(ContactType.EMAIL);
        when(dto.getContacts()).thenReturn(List.of(contactDTO));

        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        Airport result = airportService.updateAirportDetails("LAX", dto);

        assertNotNull(result);
        assertNull(result.getOperationalHours());
        assertEquals(1, result.getContacts().size());
    }

    @Test
    void ensureUpdateAirportDetailsFailsIfAirportNotFound() {
        UpdateAirportDetailsRequestDTO dto = mock(UpdateAirportDetailsRequestDTO.class);
        when(airportRepository.findByIataCode_Code("ZZZ")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> airportService.updateAirportDetails("ZZZ", dto));

        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureGetAirportsGroupedByRegionSuccess() {
        Airport cdg = new Airport(
                new IATACode("CDG"), "Charles de Gaulle",
                new Location("Europe", "France", "Paris", new GPSCoordinates(48.8584, 2.2945)),
                new Timezone("UTC+01:00"));

        when(airportRepository.findAll()).thenReturn(List.of(dummyAirport, cdg));

        Map<String, List<Airport>> result = airportService.getAirportsGroupedBy("region");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("North America"));
        assertTrue(result.containsKey("Europe"));
        assertEquals(1, result.get("North America").size());
        assertEquals(1, result.get("Europe").size());
    }

    @Test
    void ensureGetAirportsGroupedByCountrySuccess() {
        when(airportRepository.findAll()).thenReturn(List.of(dummyAirport));

        Map<String, List<Airport>> result = airportService.getAirportsGroupedBy("country");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("USA"));
    }

    @Test
    void ensureGetAirportsGroupedByMultipleAirportsSameCountry() {
        Airport jfk = new Airport(
                new IATACode("JFK"), "JFK Airport",
                new Location("North America", "USA", "New York", new GPSCoordinates(40.6413, -73.7781)),
                new Timezone("UTC-04:00"));

        when(airportRepository.findAll()).thenReturn(List.of(dummyAirport, jfk));

        Map<String, List<Airport>> result = airportService.getAirportsGroupedBy("country");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(2, result.get("USA").size());
    }

    @Test
    void ensureGetAirportsGroupedByFailsForInvalidCriteria() {
        assertThrows(IllegalArgumentException.class,
                () -> airportService.getAirportsGroupedBy("invalid"));
    }

    @Test
    void ensureCreateAirportWithExplicitNullCollectionsAndPartialGPS() {
        CreateAirportRequestDTO dto = mock(CreateAirportRequestDTO.class);
        
        when(dto.getIataCode()).thenReturn("NUL");
        when(dto.getName()).thenReturn("Null Airport");
        when(dto.getRegion()).thenReturn("Europe");
        when(dto.getCountry()).thenReturn("Portugal");
        when(dto.getCity()).thenReturn("Porto");
        when(dto.getTimezone()).thenReturn("UTC+00:00");
        
        when(dto.getLatitude()).thenReturn(41.0);
        org.mockito.Mockito.lenient().when(dto.getLongitude()).thenReturn(null);

        when(dto.getRunways()).thenReturn(null);
        when(dto.getPhotos()).thenReturn(null);
        when(dto.getTerminals()).thenReturn(null);

        when(airportRepository.findByIataCode_Code("NUL")).thenReturn(Optional.empty());
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        Airport result = airportService.createAirport(dto);

        assertNotNull(result);
        assertNotNull(result.getLocation());
        assertTrue(result.getRunways().isEmpty());
        assertTrue(result.getPhotos().isEmpty());
        assertTrue(result.getTerminals().isEmpty());
    }

    @Test
    void ensureCreateAirportWithTerminalButNullGatesAndServices() {
        CreateTerminalRequestDTO terminalDTO = new CreateTerminalRequestDTO();
        terminalDTO.setDesignation("T2");
        terminalDTO.setGates(null);
        terminalDTO.setServices(null);

        CreateAirportRequestDTO dto = mock(CreateAirportRequestDTO.class);
        when(dto.getIataCode()).thenReturn("TML");
        when(dto.getName()).thenReturn("Terminal Airport");
        when(dto.getRegion()).thenReturn("Europe");
        when(dto.getCountry()).thenReturn("Portugal");
        when(dto.getCity()).thenReturn("Lisbon");
        when(dto.getTimezone()).thenReturn("UTC+00:00");
        
        when(dto.getTerminals()).thenReturn(List.of(terminalDTO));
        
        when(dto.getRunways()).thenReturn(null);
        when(dto.getPhotos()).thenReturn(null);
        
        when(airportRepository.findByIataCode_Code("TML")).thenReturn(Optional.empty());
        when(airportRepository.save(any(Airport.class))).thenAnswer(inv -> inv.getArgument(0));

        Airport result = airportService.createAirport(dto);

        assertNotNull(result);
        assertEquals(1, result.getTerminals().size());
        assertTrue(result.getTerminals().get(0).getGates().isEmpty(), "As gates devem inicializar vazias");
        assertTrue(result.getTerminals().get(0).getServices().isEmpty(), "Os serviços devem inicializar vazios");
    }
}