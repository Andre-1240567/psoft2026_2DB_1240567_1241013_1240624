package pt.isep.psoft.alsafe.airportmanagement.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.CreateAirportRequestDTO;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;

import java.util.Optional;

import pt.isep.psoft.alsafe.airportmanagement.api.dto.UpdateAirportDetailsRequestDTO;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.OperationalHoursDTO;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.ContactDTO;
import java.util.ArrayList;
import java.util.List;

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

//    @Test
//    void ensureAddAirplaneCertificationSuccess() {
//        String modelName = "737 MAX";
//        String iata = "LAX";
//
//        when(aircraftModelRepository.findByModelName(modelName))
//                .thenReturn(Optional.of(mock(AircraftModel.class)));
//
//        when(airportRepository.findByIataCode_Code(iata))
//                .thenReturn(Optional.of(dummyAirport));
//
//        when(airportRepository.save(any(Airport.class))).thenReturn(dummyAirport);
//
//        Airport updatedAirport = airportService.addAirplaneCertification(iata, modelName);
//
//        assertNotNull(updatedAirport);
//        assertEquals(1, updatedAirport.getCertifications().size());
//        verify(aircraftModelRepository, times(1)).findByModelName(modelName);
//    }

    @Test
    void ensureAddAirplaneCertificationFailsIfModelDoesNotExist() {
        String missingModel = "GHOST-PLANE";

        when(aircraftModelRepository.findByModelName(missingModel)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> airportService.addAirplaneCertification("LAX", missingModel));

        verify(airportRepository, never()).save(any());
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
        verify(airportRepository, times(1)).save(any(Airport.class));
    }

    @Test
    void ensureCreateAirportFailsIfAlreadyExists() {
        CreateAirportRequestDTO dto = mock(CreateAirportRequestDTO.class);
        when(dto.getIataCode()).thenReturn("LAX");

        when(airportRepository.findByIataCode_Code("LAX")).thenReturn(Optional.of(dummyAirport));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> airportService.createAirport(dto));

        assertTrue(exception.getMessage().contains("already exists"));

        verify(airportRepository, never()).save(any());
    }


    @Test
    void ensureChangeOperationalStatusSuccess() {
        String iata = "LAX";
        String newStatus = "UNDER_MAINTENANCE";

        when(airportRepository.findByIataCode_Code(iata)).thenReturn(Optional.of(dummyAirport));
        when(airportRepository.save(any(Airport.class))).thenReturn(dummyAirport);

        Airport result = airportService.changeOperationalStatus(iata, newStatus);

        assertNotNull(result);
        verify(airportRepository, times(1)).save(dummyAirport);
    }

    @Test
    void ensureChangeOperationalStatusFailsIfInvalidStatus() {
        // Arrange
        String iata = "LAX";
        String invalidStatus = "ABERTO_AS_MOSCAS";

        when(airportRepository.findByIataCode_Code(iata)).thenReturn(Optional.of(dummyAirport));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> airportService.changeOperationalStatus(iata, invalidStatus));

        assertTrue(exception.getMessage().contains("Invalid status"));

        verify(airportRepository, never()).save(any());
    }

    @Test
    void ensureGetAirportsGroupedByRegionSuccess() {
        // Arrange
        List<Airport> airports = new ArrayList<>();
        airports.add(dummyAirport); // LAX (North America)

        GPSCoordinates coordinates2 = new GPSCoordinates(48.8584, 2.2945);
        Location location2 = new Location("Europe", "France", "Paris", coordinates2);
        Airport dummyAirport2 = new Airport(new IATACode("CDG"), "Paris Charles de Gaulle", location2, new Timezone("UTC+01:00"));
        airports.add(dummyAirport2);

        when(airportRepository.findAll()).thenReturn(airports);

        // Act
        java.util.Map<String, List<Airport>> result = airportService.getAirportsGroupedBy("region");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("North America"));
        assertTrue(result.containsKey("Europe"));
    }

    @Test
    void ensureGetAirportsGroupedByCountrySuccess() {
        // Arrange
        List<Airport> airports = new ArrayList<>();
        airports.add(dummyAirport); // USA

        when(airportRepository.findAll()).thenReturn(airports);

        // Act
        java.util.Map<String, List<Airport>> result = airportService.getAirportsGroupedBy("country");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("USA"));
    }

    @Test
    void ensureGetAirportsGroupedByFailsForInvalidCriteria() {
        assertThrows(IllegalArgumentException.class, () -> airportService.getAirportsGroupedBy("invalid"));
    }

    @Test
    void ensureGetAirportDetailsSuccess() {
        String iata = "LAX";
        when(airportRepository.findByIataCode_Code(iata)).thenReturn(Optional.of(dummyAirport));

        Airport result = airportService.getAirportDetails(iata);

        assertNotNull(result);
        assertEquals(dummyAirport, result);
        verify(airportRepository, times(1)).findByIataCode_Code(iata);
    }

    @Test
    void ensureGetAirportDetailsFailsIfNotFound() {
        String iata = "UNKNOWN";
        when(airportRepository.findByIataCode_Code(iata)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> airportService.getAirportDetails(iata));

        assertTrue(exception.getMessage().contains("not found"));
        verify(airportRepository, times(1)).findByIataCode_Code(iata);
    }

    @Test
    void ensureSearchAirportsByCitySuccess() {
        String city = "Los Angeles";
        List<Airport> expectedAirports = new ArrayList<>();
        expectedAirports.add(dummyAirport);

        when(airportRepository.findByLocation_City(city)).thenReturn(expectedAirports);

        List<Airport> result = airportService.searchAirportsByCity(city);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(dummyAirport, result.get(0));
        verify(airportRepository, times(1)).findByLocation_City(city);
    }

    @Test
    void ensureSearchAirportsByCityReturnsEmptyList() {
        String city = "Nowhere";

        when(airportRepository.findByLocation_City(city)).thenReturn(new ArrayList<>());

        List<Airport> result = airportService.searchAirportsByCity(city);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(airportRepository, times(1)).findByLocation_City(city);
    }

    @Test
    void ensureUpdateAirportDetailsSuccess() {
        String iata = "LAX";
        UpdateAirportDetailsRequestDTO dto = mock(UpdateAirportDetailsRequestDTO.class);
        
        OperationalHoursDTO opHoursDTO = mock(OperationalHoursDTO.class);
        when(opHoursDTO.getOpeningTime()).thenReturn("08:00");
        when(opHoursDTO.getClosingTime()).thenReturn("22:00");
        when(dto.getOperationalHours()).thenReturn(opHoursDTO);
        
        ContactDTO contactDTO = mock(ContactDTO.class);
        when(contactDTO.getValue()).thenReturn("123456789");
        when(contactDTO.getDepartment()).thenReturn("Administration");
        when(contactDTO.getType()).thenReturn(ContactType.PHONE);
        
        List<ContactDTO> contactsDTO = new ArrayList<>();
        contactsDTO.add(contactDTO);
        when(dto.getContacts()).thenReturn(contactsDTO);
        
        when(airportRepository.findByIataCode_Code(iata)).thenReturn(Optional.of(dummyAirport));
        when(airportRepository.save(any(Airport.class))).thenReturn(dummyAirport);
        
        Airport result = airportService.updateAirportDetails(iata, dto);
        
        assertNotNull(result);
        verify(airportRepository, times(1)).findByIataCode_Code(iata);
        verify(airportRepository, times(1)).save(dummyAirport);
    }
}