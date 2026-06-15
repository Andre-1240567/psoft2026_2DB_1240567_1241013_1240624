package pt.isep.psoft.alsafe.flightroutes.services;

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
import pt.isep.psoft.alsafe.flightroutes.api.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteModelAssembler;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.services.strategy.*;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightRouteServiceTest {

    @Mock private FlightRouteRepository routeRepository;
    @Mock private AirportService airportService;
    @Mock private FlightRouteModelAssembler assembler;
    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    // Built manually so the real strategy logic is exercised (not mocked)
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

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

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
    // createFlightRoute
    // -----------------------------------------------------------------------

    @Test
    void ensureRouteIsCreatedSuccessfully() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        Airport origin      = createFakeAirport("OPO", Status.OPERATIONAL);
        Airport destination = createFakeAirport("MAD", Status.OPERATIONAL);

        when(airportService.getAirportDetails("OPO")).thenReturn(origin);
        when(airportService.getAirportDetails("MAD")).thenReturn(destination);
        when(routeRepository.save(any(FlightRoute.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assembler.toModel(any(FlightRoute.class))).thenAnswer(i -> fakeDto((FlightRoute) i.getArguments()[0]));

        FlightRouteResponseDTO result = flightRouteService.createFlightRoute(dto);

        assertNotNull(result);
        assertEquals("OPO",              result.getOriginIataCode());
        assertEquals("MAD",              result.getDestinationIataCode());
        assertEquals(500.0,              result.getDistance());
        assertEquals(60,                 result.getEstimatedFlightTime());
        assertEquals(RouteStatus.ACTIVE, result.getRouteStatus());

        verify(airportService, times(1)).getAirportDetails("OPO");
        verify(airportService, times(1)).getAirportDetails("MAD");
        verify(routeRepository, times(1)).save(any(FlightRoute.class));
    }

    @Test
    void ensureExceptionIsThrownWhenOriginAirportIsNotOperational() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        when(airportService.getAirportDetails("OPO")).thenReturn(createFakeAirport("OPO", Status.CLOSED));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                flightRouteService.createFlightRoute(dto));

        assertTrue(ex.getMessage().contains("is not operational"));
        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionIsThrownWhenOriginAirportDoesNotExist_viaResourceNotFoundException() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("XXX");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        when(airportService.getAirportDetails("XXX"))
                .thenThrow(new ResourceNotFoundException("Airport not found: XXX"));

        assertThrows(ResourceNotFoundException.class, () ->
                flightRouteService.createFlightRoute(dto));

        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionIsThrownWhenOriginAirportDoesNotExist_viaIllegalArgument() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("XXX");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        when(airportService.getAirportDetails("XXX"))
                .thenThrow(new IllegalArgumentException("Airport not found: XXX"));

        assertThrows(ResourceNotFoundException.class, () ->
                flightRouteService.createFlightRoute(dto));

        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureExceptionIsThrownWhenDestinationAirportDoesNotExist() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("XXX");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        when(airportService.getAirportDetails("OPO")).thenReturn(createFakeAirport("OPO", Status.OPERATIONAL));
        when(airportService.getAirportDetails("XXX"))
                .thenThrow(new ResourceNotFoundException("Airport not found: XXX"));

        assertThrows(ResourceNotFoundException.class, () ->
                flightRouteService.createFlightRoute(dto));

        verify(routeRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // getRouteById
    // -----------------------------------------------------------------------

    @Test
    void ensureGetRouteByIdReturnsRouteWhenFound() {
        FlightRoute route = createFakeRoute("route-001", "OPO", "LIS");
        FlightRouteResponseDTO dto = fakeDto(route);

        when(routeRepository.findByIdWithHistory("route-001")).thenReturn(Optional.of(route));
        when(assembler.toModel(route)).thenReturn(dto);

        FlightRouteResponseDTO result = flightRouteService.getRouteById("route-001");

        assertNotNull(result);
        assertEquals("route-001", result.getRouteId());
        verify(routeRepository, times(1)).findByIdWithHistory("route-001");
    }

    @Test
    void ensureGetRouteByIdThrowsWhenRouteDoesNotExist() {
        when(routeRepository.findByIdWithHistory("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                flightRouteService.getRouteById("nonexistent"));
    }

    // -----------------------------------------------------------------------
    // searchRoutes — exercises the real Strategy implementations
    // -----------------------------------------------------------------------

    @Test
    void ensureSearchRoutesReturnsPaginatedResultsForNoFilters() {
        Pageable pageable = PageRequest.of(0, 5);
        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        FlightRoute r2 = createFakeRoute("r2", "OPO", "MAD");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1, r2));

        // Calcular o DTO antes
        FlightRouteResponseDTO dto1 = fakeDto(r1);
        FlightRouteResponseDTO dto2 = fakeDto(r2);

        when(routeRepository.findAll(pageable)).thenReturn(page);
        when(assembler.toModel(r1)).thenReturn(dto1);
        when(assembler.toModel(r2)).thenReturn(dto2);

        Page<FlightRouteResponseDTO> result = flightRouteService.searchRoutes(null, null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(routeRepository, times(1)).findAll(pageable);
        verify(routeRepository, never()).findByOrigin_IataCode_Code(any(), any());
    }

    @Test
    void ensureSearchByOriginIataReturnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 5);
        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        FlightRoute r2 = createFakeRoute("r2", "OPO", "MAD");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1, r2));

        // Calcular o DTO antes
        FlightRouteResponseDTO dto1 = fakeDto(r1);
        FlightRouteResponseDTO dto2 = fakeDto(r2);

        when(routeRepository.findByOrigin_IataCode_Code("OPO", pageable)).thenReturn(page);
        when(assembler.toModel(r1)).thenReturn(dto1);
        when(assembler.toModel(r2)).thenReturn(dto2);

        Page<FlightRouteResponseDTO> result = flightRouteService.searchRoutes("OPO", null, pageable);

        assertEquals(2, result.getContent().size());
        verify(routeRepository, times(1)).findByOrigin_IataCode_Code("OPO", pageable);
        verify(routeRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void ensureSearchByDestinationIataReturnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 5);
        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1));

        // Calcular o DTO antes
        FlightRouteResponseDTO dto1 = fakeDto(r1);

        when(routeRepository.findByDestination_IataCode_Code("LIS", pageable)).thenReturn(page);
        when(assembler.toModel(r1)).thenReturn(dto1);

        Page<FlightRouteResponseDTO> result = flightRouteService.searchRoutes(null, "LIS", pageable);

        assertEquals(1, result.getContent().size());
        verify(routeRepository, times(1)).findByDestination_IataCode_Code("LIS", pageable);
        verify(routeRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void ensureSearchByBothOriginAndDestinationReturnsPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 5);
        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1));

        // Calcular o DTO antes
        FlightRouteResponseDTO dto1 = fakeDto(r1);

        when(routeRepository.findByOrigin_IataCode_CodeAndDestination_IataCode_Code("OPO", "LIS", pageable))
                .thenReturn(page);
        when(assembler.toModel(r1)).thenReturn(dto1);

        Page<FlightRouteResponseDTO> result = flightRouteService.searchRoutes("OPO", "LIS", pageable);

        assertEquals(1, result.getContent().size());
        verify(routeRepository, times(1))
                .findByOrigin_IataCode_CodeAndDestination_IataCode_Code("OPO", "LIS", pageable);
        verify(routeRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void ensureSearchIsCaseInsensitive() {
        Pageable pageable = PageRequest.of(0, 5);
        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1));

        // Calcular o DTO antes
        FlightRouteResponseDTO dto1 = fakeDto(r1);

        // Service must uppercase "opo" → "OPO" before querying
        when(routeRepository.findByOrigin_IataCode_Code("OPO", pageable)).thenReturn(page);
        when(assembler.toModel(r1)).thenReturn(dto1);

        Page<FlightRouteResponseDTO> result = flightRouteService.searchRoutes("opo", null, pageable);

        assertEquals(1, result.getContent().size());
        verify(routeRepository, times(1)).findByOrigin_IataCode_Code("OPO", pageable);
    }

    // -----------------------------------------------------------------------
    // updateRoute
    // -----------------------------------------------------------------------

    @Test
    void ensureRouteIsUpdatedSuccessfully() {
        FlightRoute route = createFakeRoute("route-001", "OPO", "LIS");
        when(routeRepository.findById("route-001")).thenReturn(Optional.of(route));
        when(routeRepository.save(any(FlightRoute.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assembler.toModel(any(FlightRoute.class))).thenAnswer(i -> fakeDto((FlightRoute) i.getArguments()[0]));

        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(600.0);
        dto.setEstimatedFlightTime(75);
        dto.setMinRangeRequired(700.0);
        dto.setMinCapacityRequired(180);
        dto.setVersion(0L);

        FlightRouteResponseDTO result = flightRouteService.updateRoute("route-001", dto);

        assertNotNull(result);
        assertEquals(600.0, result.getDistance());
        assertEquals(75,    result.getEstimatedFlightTime());
        verify(routeRepository, times(1)).save(any(FlightRoute.class));
    }

    @Test
    void ensureUpdateThrowsWhenVersionMismatches() {
        FlightRoute route = createFakeRoute("route-001", "OPO", "LIS");
        when(routeRepository.findById("route-001")).thenReturn(Optional.of(route));

        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(600.0);
        dto.setEstimatedFlightTime(75);
        dto.setMinRangeRequired(700.0);
        dto.setMinCapacityRequired(180);
        dto.setVersion(999L);  // intentionally stale

        assertThrows(org.springframework.orm.ObjectOptimisticLockingFailureException.class, () ->
                flightRouteService.updateRoute("route-001", dto));

        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureUpdateThrowsWhenRouteDoesNotExist() {
        when(routeRepository.findById("nonexistent")).thenReturn(Optional.empty());

        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(600.0);
        dto.setEstimatedFlightTime(75);
        dto.setMinRangeRequired(700.0);
        dto.setMinCapacityRequired(180);
        dto.setVersion(0L);

        assertThrows(ResourceNotFoundException.class, () ->
                flightRouteService.updateRoute("nonexistent", dto));

        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureUpdateThrowsWhenRouteIsDeactivated() {
        FlightRoute route = createFakeRoute("route-001", "OPO", "LIS");
        route.deactivate("atcc_jose");
        when(routeRepository.findById("route-001")).thenReturn(Optional.of(route));

        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(600.0);
        dto.setEstimatedFlightTime(75);
        dto.setMinRangeRequired(700.0);
        dto.setMinCapacityRequired(180);
        dto.setVersion(0L);

        assertThrows(IllegalStateException.class, () ->
                flightRouteService.updateRoute("route-001", dto));

        verify(routeRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // deactivateRoute
    // -----------------------------------------------------------------------

    @Test
    void ensureRouteIsDeactivatedSuccessfully() {
        FlightRoute route = createFakeRoute("route-001", "OPO", "LIS");
        when(routeRepository.findById("route-001")).thenReturn(Optional.of(route));
        when(routeRepository.save(any(FlightRoute.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assembler.toModel(any(FlightRoute.class))).thenAnswer(i -> fakeDto((FlightRoute) i.getArguments()[0]));

        FlightRouteResponseDTO result = flightRouteService.deactivateRoute("route-001");

        assertEquals(RouteStatus.DEACTIVATED, result.getRouteStatus());
        verify(routeRepository, times(1)).save(any(FlightRoute.class));
    }

    @Test
    void ensureDeactivateThrowsWhenRouteDoesNotExist() {
        when(routeRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                flightRouteService.deactivateRoute("nonexistent"));

        verify(routeRepository, never()).save(any());
    }

    @Test
    void ensureDeactivateThrowsWhenRouteIsAlreadyDeactivated() {
        FlightRoute route = createFakeRoute("route-001", "OPO", "LIS");
        route.deactivate("atcc_jose");
        when(routeRepository.findById("route-001")).thenReturn(Optional.of(route));

        assertThrows(IllegalStateException.class, () ->
                flightRouteService.deactivateRoute("route-001"));

        verify(routeRepository, never()).save(any());
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