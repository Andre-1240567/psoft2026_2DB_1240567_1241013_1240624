package pt.isep.psoft.alsafe.airportmanagement.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import pt.isep.psoft.alsafe.airportmanagement.api.dto.BusiestAirportDTO;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteModelAssembler;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;
import pt.isep.psoft.alsafe.flightroutes.services.strategy.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportFlightRouteServiceTest {

    @Mock private FlightRouteRepository routeRepository;
    @Mock private AirportService airportService;
    @Mock private FlightRouteModelAssembler assembler;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    private FlightRouteService flightRouteService;

    @BeforeEach
    void setUp() {
        // Security context
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getName()).thenReturn("atcc_jose");
        lenient().when(authentication.getPrincipal()).thenReturn("atcc_jose");
        SecurityContextHolder.setContext(securityContext);

        // Build the service with real strategy implementations (backed by the mocked repository)
        List<RouteSearchStrategy> strategies = List.of(
                new SearchByBothStrategy(routeRepository),
                new SearchByOriginStrategy(routeRepository),
                new SearchByDestinationStrategy(routeRepository),
                new SearchAllStrategy(routeRepository)
        );

        flightRouteService = new FlightRouteService(
                routeRepository, airportService, assembler, strategies);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Airport createFakeAirport(String iata, Status status) {
        Airport airport = new Airport(
                new IATACode(iata),
                "Fake Airport",
                new Location("Reg", "Country", "City", new GPSCoordinates(0.0, 0.0)),
                new Timezone("UTC+00:00"));
        if (status != null) {
            airport.changeStatus(status);
        }
        return airport;
    }

    private FlightRoute createFakeRoute(String id, String originIata, String destIata) {
        Airport origin      = createFakeAirport(originIata, Status.OPERATIONAL);
        Airport destination = createFakeAirport(destIata,  Status.OPERATIONAL);
        RouteRequirement req = new RouteRequirement(600.0, 150);
        FlightRoute route = new FlightRoute(id, origin, destination, 500.0, 60, req, "atcc_jose");

        // Simulate a JPA-persisted entity that already has version = 0
        FlightRoute spyRoute = spy(route);
        lenient().when(spyRoute.getVersion()).thenReturn(0L);
        return spyRoute;
    }

    private FlightRouteResponseDTO fakeDto(FlightRoute route) {
        return new FlightRouteResponseDTO(route);
    }

    // -----------------------------------------------------------------------
    // US209: getRoutesByAirport
    // -----------------------------------------------------------------------

    @Test
    void ensureGetRoutesByAirportSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        FlightRoute r1 = createFakeRoute("r1", "LAX", "JFK");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1));

        when(airportService.getAirportDetails("LAX")).thenReturn(createFakeAirport("LAX", Status.OPERATIONAL));
        when(routeRepository.findByOrigin_IataCode_CodeOrDestination_IataCode_Code("LAX", "LAX", pageable)).thenReturn(page);
        
        FlightRouteResponseDTO dto1 = fakeDto(r1);
        when(assembler.toModel(r1)).thenReturn(dto1);

        Page<FlightRouteResponseDTO> result = flightRouteService.getRoutesByAirport("lax", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(routeRepository).findByOrigin_IataCode_CodeOrDestination_IataCode_Code("LAX", "LAX", pageable);
    }

    // -----------------------------------------------------------------------
    // US210: getBusiestAirports
    // -----------------------------------------------------------------------

    @Test
    void ensureGetBusiestAirportsSuccess() {
        Object[] row1 = new Object[]{"LAX", 10L};
        Object[] row2 = new Object[]{"JFK", 8L};
        List<Object[]> queryResults = List.of(row1, row2);

        when(routeRepository.findBusiestAirportsStatistics()).thenReturn(queryResults);

        List<BusiestAirportDTO> result = flightRouteService.getBusiestAirports();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("LAX", result.get(0).getIataCode());
        assertEquals(10L, result.get(0).getRouteCount());
    }
}