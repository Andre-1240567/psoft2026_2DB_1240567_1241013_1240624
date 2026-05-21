package pt.isep.psoft.alsafe.flightroutes.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

import pt.isep.psoft.alsafe.flightroutes.api.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightRouteServiceTest {

    @Mock
    private FlightRouteRepository routeRepository;

    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private FlightRouteService flightRouteService;

    private Airport createFakeAirport(String iata) {
        return new Airport(new IATACode(iata), "Fake Airport", 
               new Location("Reg", "Country", "City", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
    }

    @Test
    void ensureRouteIsCreatedSuccessfully() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        Airport origin = createFakeAirport("OPO");
        Airport destination = createFakeAirport("MAD");

        // CORREÇÃO: findByIataCode_Code
        when(airportRepository.findByIataCode_Code("OPO")).thenReturn(Optional.of(origin));
        when(airportRepository.findByIataCode_Code("MAD")).thenReturn(Optional.of(destination));
        
        when(routeRepository.save(any(FlightRoute.class))).thenAnswer(i -> i.getArguments()[0]);

        FlightRoute createdRoute = flightRouteService.createFlightRoute(dto);

        assertNotNull(createdRoute);
        // CORREÇÃO: getCode()
        assertEquals("OPO", createdRoute.getOrigin().getIataCode().getCode());
        assertEquals("MAD", createdRoute.getDestination().getIataCode().getCode());
        
        verify(airportRepository, times(1)).findByIataCode_Code("OPO");
        verify(routeRepository, times(1)).save(any(FlightRoute.class));
    }

    @Test
    void ensureExceptionIsThrownWhenOriginIsInvalid() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("XXX"); 
        dto.setDestinationIata("MAD");

        when(airportRepository.findByIataCode_Code("XXX")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            flightRouteService.createFlightRoute(dto);
        });

        assertEquals("Origin airport not found: XXX", exception.getMessage());
        
        verify(routeRepository, never()).save(any());
    }
}