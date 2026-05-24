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
}