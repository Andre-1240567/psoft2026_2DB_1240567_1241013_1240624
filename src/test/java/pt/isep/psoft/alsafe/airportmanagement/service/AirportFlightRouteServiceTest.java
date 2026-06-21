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
import pt.isep.psoft.alsafe.flightroutes.api.dto.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;
import pt.isep.psoft.alsafe.flightroutes.services.strategy.*;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AirportFlightRouteServiceTest {

    @Mock private FlightRouteRepository routeRepository;
    @Mock private AirportService airportService;
    @Mock private FlightRouteModelAssembler assembler;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;
    @Mock private ScheduledFlightRepository scheduledFlightRepository;

    private FlightRouteService flightRouteService;

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getName()).thenReturn("atcc_jose");
        lenient().when(authentication.getPrincipal()).thenReturn("atcc_jose");
        SecurityContextHolder.setContext(securityContext);

        List<RouteSearchStrategy> strategies = List.of(
                new SearchByBothStrategy(routeRepository),
                new SearchByOriginStrategy(routeRepository),
                new SearchByDestinationStrategy(routeRepository),
                new SearchAllStrategy(routeRepository)
        );

        flightRouteService = new FlightRouteService(
                routeRepository, scheduledFlightRepository, airportService,
                assembler, strategies, List.of());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private Airport createFakeAirport(String iata, Status status) {
        Airport airport = new Airport(
                new IATACode(iata), "Fake Airport",
                new Location("Reg", "Country", "City", new GPSCoordinates(0.0, 0.0)),
                new Timezone("UTC+00:00"));
        if (status != null) {
            airport.changeStatus(status);
        }
        return airport;
    }

    private FlightRoute createFakeRoute(String id, String originIata, String destIata) {
        Airport origin = createFakeAirport(originIata, null);
        Airport dest   = createFakeAirport(destIata,   null);
        
        return new FlightRoute(id, origin, dest, 500.0, 60,
                new RouteRequirement(600.0, 150), "atcc_jose");
    }

    @Test
    void ensureGetRoutesByAirportSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        FlightRoute r1 = createFakeRoute("r1", "LAX", "JFK");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1));

        when(airportService.getAirportDetails("LAX")).thenReturn(createFakeAirport("LAX", null));
        when(routeRepository.findByOrigin_IataCode_CodeOrDestination_IataCode_Code("LAX", "LAX", pageable))
                .thenReturn(page);
        when(assembler.toModel(r1)).thenReturn(new FlightRouteResponseDTO(r1));

        Page<FlightRouteResponseDTO> result = flightRouteService.getRoutesByAirport("lax", pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(routeRepository)
                .findByOrigin_IataCode_CodeOrDestination_IataCode_Code("LAX", "LAX", pageable);
    }

    @Test
    void ensureGetRoutesByAirportNormalisesIataCodeToUpperCase() {
        Pageable pageable = PageRequest.of(0, 10);
        when(airportService.getAirportDetails("OPO")).thenReturn(createFakeAirport("OPO", null));
        when(routeRepository.findByOrigin_IataCode_CodeOrDestination_IataCode_Code("OPO", "OPO", pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<FlightRouteResponseDTO> result = flightRouteService.getRoutesByAirport("opo", pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        verify(routeRepository)
                .findByOrigin_IataCode_CodeOrDestination_IataCode_Code("OPO", "OPO", pageable);
    }

    @Test
    void ensureGetRoutesByAirportThrowsIfAirportNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(airportService.getAirportDetails("ZZZ"))
                .thenThrow(new ResourceNotFoundException("Airport with the code ZZZ not found."));

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> flightRouteService.getRoutesByAirport("zzz", pageable));

        assertTrue(ex.getMessage().contains("not found"));
        verify(routeRepository, never())
                .findByOrigin_IataCode_CodeOrDestination_IataCode_Code(any(), any(), any());
    }

    @Test
    void ensureGetRoutesByAirportReturnsEmptyPageWhenNoRoutes() {
        Pageable pageable = PageRequest.of(0, 10);
        when(airportService.getAirportDetails("OPO")).thenReturn(createFakeAirport("OPO", null));
        when(routeRepository.findByOrigin_IataCode_CodeOrDestination_IataCode_Code("OPO", "OPO", pageable))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<FlightRouteResponseDTO> result = flightRouteService.getRoutesByAirport("OPO", pageable);

        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void ensureGetBusiestAirportsSuccess() {
        List<Object[]> queryResults = new java.util.ArrayList<>();
        queryResults.add(new Object[]{"LAX", 10L});
        queryResults.add(new Object[]{"JFK", 8L});
        when(routeRepository.findBusiestAirportsStatistics()).thenReturn(queryResults);

        List<BusiestAirportDTO> result = flightRouteService.getBusiestAirports();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("LAX", result.get(0).getIataCode());
        assertEquals(10L, result.get(0).getRouteCount());
        assertEquals("JFK", result.get(1).getIataCode());
        assertEquals(8L,   result.get(1).getRouteCount());
    }

    @Test
    void ensureGetBusiestAirportsReturnsEmptyListWhenNoRoutes() {
        when(routeRepository.findBusiestAirportsStatistics()).thenReturn(Collections.emptyList());

        List<BusiestAirportDTO> result = flightRouteService.getBusiestAirports();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void ensureGetBusiestAirportsReturnsSingleEntry() {
        List<Object[]> queryResults = new java.util.ArrayList<>();
        queryResults.add(new Object[]{"LIS", 3L});
        when(routeRepository.findBusiestAirportsStatistics()).thenReturn(queryResults);

        List<BusiestAirportDTO> result = flightRouteService.getBusiestAirports();

        assertEquals(1, result.size());
        assertEquals("LIS", result.get(0).getIataCode());
        assertEquals(3L,    result.get(0).getRouteCount());
    }
}