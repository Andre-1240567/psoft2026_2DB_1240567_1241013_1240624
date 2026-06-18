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

import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;
import pt.isep.psoft.alsafe.flightroutes.api.AlternativeRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteModelAssembler;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.RouteUtilizationDTO;
import pt.isep.psoft.alsafe.flightroutes.api.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.services.routing.AlternativeRoutingStrategy;
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
    @Mock private pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository scheduledFlightRepository;


    @Mock private AlternativeRoutingStrategy mockedRoutingStrategy;

    private FlightRouteService flightRouteService;

    @BeforeEach
    void setUp() {
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.isAuthenticated()).thenReturn(true);
        lenient().when(authentication.getName()).thenReturn("atcc_jose");
        SecurityContextHolder.setContext(securityContext);

        List<RouteSearchStrategy> searchStrategies = List.of(
                new SearchByBothStrategy(routeRepository),
                new SearchByOriginStrategy(routeRepository),
                new SearchByDestinationStrategy(routeRepository),
                new SearchAllStrategy(routeRepository)
        );

        List<AlternativeRoutingStrategy> routingStrategies = List.of(mockedRoutingStrategy);

        flightRouteService = new FlightRouteService(
                routeRepository, scheduledFlightRepository, airportService, assembler, searchStrategies, routingStrategies);
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
        Airport origin      = createFakeAirport(originIata, null);
        Airport destination = createFakeAirport(destIata,  null);
        RouteRequirement req = new RouteRequirement(600.0, 150);
        FlightRoute route = new FlightRoute(id, origin, destination, 500.0, 60, req, "atcc_jose");

        FlightRoute spyRoute = spy(route);
        lenient().when(spyRoute.getVersion()).thenReturn(0L);
        return spyRoute;
    }

    private FlightRouteResponseDTO fakeDto(FlightRoute route) {
        return new FlightRouteResponseDTO(route);
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

        Airport origin      = createFakeAirport("OPO", null);
        Airport destination = createFakeAirport("MAD", null);

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

        when(airportService.getAirportDetails("OPO")).thenReturn(createFakeAirport("OPO", null));
        when(airportService.getAirportDetails("XXX"))
                .thenThrow(new ResourceNotFoundException("Airport not found: XXX"));

        assertThrows(ResourceNotFoundException.class, () ->
                flightRouteService.createFlightRoute(dto));

        verify(routeRepository, never()).save(any());
    }

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

    @Test
    void ensureSearchRoutesReturnsPaginatedResultsForNoFilters() {
        Pageable pageable = PageRequest.of(0, 5);
        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        FlightRoute r2 = createFakeRoute("r2", "OPO", "MAD");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1, r2));

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

        FlightRouteResponseDTO dto1 = fakeDto(r1);

        when(routeRepository.findByOrigin_IataCode_Code("OPO", pageable)).thenReturn(page);
        when(assembler.toModel(r1)).thenReturn(dto1);

        Page<FlightRouteResponseDTO> result = flightRouteService.searchRoutes("opo", null, pageable);

        assertEquals(1, result.getContent().size());
        verify(routeRepository, times(1)).findByOrigin_IataCode_Code("OPO", pageable);
    }

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
        dto.setVersion(999L);

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


    @Test
    void ensureGetActiveRoutesSortedWorksForDistance() {
        Pageable pageable = PageRequest.of(0, 5);
        
        Pageable sortedPageable = PageRequest.of(0, 5, org.springframework.data.domain.Sort.by("distance").ascending());

        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1));

        FlightRouteResponseDTO dto1 = fakeDto(r1);

        when(routeRepository.findByRouteStatus(RouteStatus.ACTIVE, sortedPageable)).thenReturn(page);
        when(assembler.toModel(r1)).thenReturn(dto1);

        Page<FlightRouteResponseDTO> result = flightRouteService.getActiveRoutesSorted(RouteStatus.ACTIVE, "distance", pageable);

        assertEquals(1, result.getContent().size());
        verify(routeRepository).findByRouteStatus(RouteStatus.ACTIVE, sortedPageable);
    }

    @Test
    void ensureGetActiveRoutesSortedThrowsWhenSortIsInvalid() {
        Pageable pageable = PageRequest.of(0, 5);
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                flightRouteService.getActiveRoutesSorted(RouteStatus.ACTIVE, "invalid_sort", pageable));
                
        assertEquals("Invalid sort parameter. Use 'popularity' or 'distance'.", ex.getMessage());
        
        verify(routeRepository, never()).findActiveRoutesByPopularity(any(), any());
        verify(routeRepository, never()).findByRouteStatus(any(), any());
    }

    @Test
    void ensureGetActiveRoutesSortedWorksForPopularity() {
        Pageable pageable = PageRequest.of(0, 5);
        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        Page<FlightRoute> page = new PageImpl<>(List.of(r1));
        FlightRouteResponseDTO dto1 = fakeDto(r1);

        when(routeRepository.findActiveRoutesByPopularity(RouteStatus.ACTIVE, pageable)).thenReturn(page);
        when(assembler.toModel(r1)).thenReturn(dto1);

        Page<FlightRouteResponseDTO> result = flightRouteService.getActiveRoutesSorted(RouteStatus.ACTIVE, "popularity", pageable);

        assertEquals(1, result.getContent().size());
        verify(routeRepository).findActiveRoutesByPopularity(RouteStatus.ACTIVE, pageable);
    }

    @Test
    void ensureTotalNetworkDistanceIsCalculated() {
        when(routeRepository.calculateTotalNetworkDistance(RouteStatus.ACTIVE)).thenReturn(15000.5);

        Double result = flightRouteService.getTotalNetworkDistance();

        assertEquals(15000.5, result);
        verify(routeRepository).calculateTotalNetworkDistance(RouteStatus.ACTIVE);
    }


    @Test
    void ensureFindAlternativeRoutesWorksAndConvertsToDTOs() {
        Airport opo = createFakeAirport("OPO", null);
        Airport lis = createFakeAirport("LIS", null);
        when(airportService.getAirportDetails("OPO")).thenReturn(opo);
        when(airportService.getAirportDetails("LIS")).thenReturn(lis);

        when(mockedRoutingStrategy.getAlgorithmName()).thenReturn("eco-friendly");
        
        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        List<FlightRoute> path = List.of(r1);
        List<List<FlightRoute>> foundPaths = List.of(path);
        
        FlightRouteResponseDTO dto1 = fakeDto(r1);
        
        when(mockedRoutingStrategy.findAlternatives("OPO", "LIS")).thenReturn(foundPaths);
        when(assembler.toModel(r1)).thenReturn(dto1);

        List<AlternativeRouteResponseDTO> result = flightRouteService.findAlternativeRoutes("OPO", "LIS", "eco-friendly");

        assertEquals(1, result.size());
        assertEquals(500.0, result.get(0).getTotalDistance());
        assertEquals(60, result.get(0).getTotalEstimatedFlightTime());
    }

    @Test
    void ensureFindAlternativeRoutesThrowsIfAlgorithmNotFound() {
        Airport opo = createFakeAirport("OPO", null);
        Airport lis = createFakeAirport("LIS", null);
        when(airportService.getAirportDetails("OPO")).thenReturn(opo);
        when(airportService.getAirportDetails("LIS")).thenReturn(lis);

        when(mockedRoutingStrategy.getAlgorithmName()).thenReturn("eco-friendly");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                flightRouteService.findAlternativeRoutes("OPO", "LIS", "unknown-algorithm"));

        assertTrue(ex.getMessage().contains("Routing algorithm not supported"));
    }



    @Test
    void ensureGetRouteHistoryWorks() {
        FlightRoute route = createFakeRoute("r1", "OPO", "MAD");
        when(routeRepository.findByIdWithHistory("r1")).thenReturn(Optional.of(route));

        List<FlightRouteResponseDTO.RouteHistoryDTO> history = flightRouteService.getRouteHistory("r1");

        assertEquals(1, history.size());
        assertEquals("atcc_jose", history.get(0).getAuthor());
    }

    @Test
    void ensureGetRouteHistoryThrowsWhenNotFound() {
        when(routeRepository.findByIdWithHistory("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> flightRouteService.getRouteHistory("nonexistent"));
    }


    @Test
    void ensureGetCompatibleRoutesWorks() {
        FlightRoute route = createFakeRoute("r1", "OPO", "MAD");
        
        FlightRouteResponseDTO dto = fakeDto(route);
        
        when(routeRepository.findCompatibleRoutes(1000.0, 150)).thenReturn(List.of(route));
        when(assembler.toModel(route)).thenReturn(dto);

        List<FlightRouteResponseDTO> result = flightRouteService.getCompatibleRoutesForAircraft(1000.0, 150);

        assertEquals(1, result.size());
        assertEquals("OPO", result.get(0).getOriginIataCode());
    }


    @Test
    void ensureSearchRoutesThrowsWhenNoStrategyMatches() {
        FlightRouteService brokenService = new FlightRouteService(
                routeRepository, scheduledFlightRepository, airportService, assembler, List.of(), List.of()
        );

        Pageable pageable = PageRequest.of(0, 5);
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> 
                brokenService.searchRoutes("OPO", "MAD", pageable)
        );
        
        assertEquals("No valid search strategy found for the provided filters.", ex.getMessage());
    }

    @Test
    void ensureCreateRouteThrowsWhenOriginStatusIsNull() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        Airport originNullStatus = createFakeAirport("OPO", null); 
        when(airportService.getAirportDetails("OPO")).thenReturn(originNullStatus);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                flightRouteService.createFlightRoute(dto));

        assertTrue(ex.getMessage().contains("is not operational"));
    }

    @Test
    void ensureGetCurrentUserReturnsSystemWhenNoAuthenticationExists() {
        SecurityContextHolder.clearContext();
        
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        Airport origin = createFakeAirport("OPO", null);
        Airport destination = createFakeAirport("MAD", null);
        when(airportService.getAirportDetails("OPO")).thenReturn(origin);
        when(airportService.getAirportDetails("MAD")).thenReturn(destination);
        
        FlightRoute fakeRoute = createFakeRoute("r1", "OPO", "MAD");
        
        FlightRouteResponseDTO finalDto = fakeDto(fakeRoute);
        
        when(routeRepository.save(any(FlightRoute.class))).thenReturn(fakeRoute);
        when(assembler.toModel(any(FlightRoute.class))).thenReturn(finalDto);

        FlightRouteResponseDTO result = flightRouteService.createFlightRoute(dto);
        assertNotNull(result);
        
        SecurityContextHolder.setContext(securityContext);
    }


    @Test
    void ensureCreateRouteThrowsWhenDestinationStatusIsNull() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        Airport origin = createFakeAirport("OPO", null);
        Airport destNullStatus = createFakeAirport("MAD", null); 
        
        when(airportService.getAirportDetails("OPO")).thenReturn(origin);
        when(airportService.getAirportDetails("MAD")).thenReturn(destNullStatus);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                flightRouteService.createFlightRoute(dto));

        assertTrue(ex.getMessage().contains("Destination airport"));
    }

    @Test
    void ensureGetCurrentUserReturnsSystemWhenAnonymousUser() {
        when(authentication.getPrincipal()).thenReturn("anonymousUser");

        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        Airport origin = createFakeAirport("OPO", null);
        Airport destination = createFakeAirport("MAD", null);
        when(airportService.getAirportDetails("OPO")).thenReturn(origin);
        when(airportService.getAirportDetails("MAD")).thenReturn(destination);
        when(routeRepository.save(any(FlightRoute.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assembler.toModel(any(FlightRoute.class))).thenAnswer(i -> fakeDto((FlightRoute) i.getArguments()[0]));

        FlightRouteResponseDTO result = flightRouteService.createFlightRoute(dto);
        assertNotNull(result);

    }

    @Test
    void ensureUpdateThrowsWhenDtoVersionIsNull() {
        FlightRoute route = createFakeRoute("route-001", "OPO", "LIS");
        when(routeRepository.findById("route-001")).thenReturn(Optional.of(route));

        UpdateFlightRouteDTO dto = new UpdateFlightRouteDTO();
        dto.setDistance(600.0);
        dto.setEstimatedFlightTime(75);
        dto.setMinRangeRequired(700.0);
        dto.setMinCapacityRequired(180);
        dto.setVersion(null);

        assertThrows(org.springframework.orm.ObjectOptimisticLockingFailureException.class, () ->
                flightRouteService.updateRoute("route-001", dto));
    }
    @Test
    void ensureGetCurrentUserReturnsSystemWhenNotAuthenticated() {
        lenient().when(authentication.isAuthenticated()).thenReturn(false);
        lenient().when(authentication.getPrincipal()).thenReturn("atcc_jose");

        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        Airport origin = createFakeAirport("OPO", null);
        Airport destination = createFakeAirport("MAD", null);
        when(airportService.getAirportDetails("OPO")).thenReturn(origin);
        when(airportService.getAirportDetails("MAD")).thenReturn(destination);
        when(routeRepository.save(any(FlightRoute.class))).thenAnswer(i -> i.getArguments()[0]);
        when(assembler.toModel(any(FlightRoute.class))).thenAnswer(i -> fakeDto((FlightRoute) i.getArguments()[0]));

        FlightRouteResponseDTO result = flightRouteService.createFlightRoute(dto);

        assertNotNull(result);
    }
    @Test
    void ensureExceptionIsThrownWhenDestinationAirportIsNotOperational() {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(500.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(600.0);
        dto.setMinCapacityRequired(150);

        Airport origin = createFakeAirport("OPO", null);
        Airport destinationClosed = createFakeAirport("MAD", Status.CLOSED);

        when(airportService.getAirportDetails("OPO")).thenReturn(origin);
        when(airportService.getAirportDetails("MAD")).thenReturn(destinationClosed);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                flightRouteService.createFlightRoute(dto));

        assertTrue(ex.getMessage().contains("Destination airport"));
        assertTrue(ex.getMessage().contains("is not operational"));
        verify(routeRepository, never()).save(any());
    }


    @Test
    void ensureGetRouteUtilizationReportReturnsMappedDTOs() {
        Object[] row1 = new Object[]{"route-1", "OPO", "LIS", 5L};
        Object[] row2 = new Object[]{"route-2", "OPO", "MAD", 3L};

        when(scheduledFlightRepository.findRouteUtilizationReport()).thenReturn(List.of(row1, row2));

        List<RouteUtilizationDTO> result = flightRouteService.getRouteUtilizationReport();

        assertEquals(2, result.size());
        assertEquals("route-1", result.get(0).getRouteId());
        assertEquals("OPO",     result.get(0).getOriginIata());
        assertEquals("LIS",     result.get(0).getDestinationIata());
        assertEquals(5L,        result.get(0).getTotalFlights());
        assertEquals("route-2", result.get(1).getRouteId());
        assertEquals(3L,        result.get(1).getTotalFlights());

        verify(scheduledFlightRepository, times(1)).findRouteUtilizationReport();
    }

    @Test
    void ensureGetRouteUtilizationReportReturnsEmptyListWhenNoFlights() {
        when(scheduledFlightRepository.findRouteUtilizationReport()).thenReturn(List.of());

        List<RouteUtilizationDTO> result = flightRouteService.getRouteUtilizationReport();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(scheduledFlightRepository, times(1)).findRouteUtilizationReport();
    }

    @Test
    void ensureExportGeoJsonContainsActiveRoutes() {
        FlightRoute route = createFakeRoute("r1", "OPO", "LIS");
        when(routeRepository.findAll()).thenReturn(List.of(route));

        String result = flightRouteService.exportGeoJson();

        assertTrue(result.contains("\"type\":\"FeatureCollection\""));
        assertTrue(result.contains("\"OPO\""));
        assertTrue(result.contains("\"LIS\""));
        assertTrue(result.contains("\"type\":\"LineString\""));
        assertTrue(result.contains("500.0"));
    }

    @Test
    void ensureExportGeoJsonExcludesDeactivatedRoutes() {
        FlightRoute active      = createFakeRoute("r1", "OPO", "LIS");
        FlightRoute deactivated = createFakeRoute("r2", "OPO", "MAD");
        deactivated.deactivate("atcc_jose");

        when(routeRepository.findAll()).thenReturn(List.of(active, deactivated));

        String result = flightRouteService.exportGeoJson();

        assertTrue(result.contains("\"OPO\""));
        assertTrue(result.contains("\"LIS\""));
        assertFalse(result.contains("\"MAD\""));
    }

    @Test
    void ensureExportGeoJsonReturnsEmptyFeatureCollectionWhenNoActiveRoutes() {
        when(routeRepository.findAll()).thenReturn(List.of());

        String result = flightRouteService.exportGeoJson();

        assertEquals("{\"type\":\"FeatureCollection\",\"features\":[]}", result);
    }


    @Test
    void ensureExportKmlContainsActiveRoutes() {
        FlightRoute route = createFakeRoute("r1", "OPO", "LIS");
        when(routeRepository.findAll()).thenReturn(List.of(route));

        String result = flightRouteService.exportKml();

        assertTrue(result.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(result.contains("<kml"));
        assertTrue(result.contains("<Placemark>"));
        assertTrue(result.contains("OPO → LIS"));
        assertTrue(result.contains("500.0 km"));
        assertTrue(result.contains("<LineString>"));
    }

    @Test
    void ensureExportKmlExcludesDeactivatedRoutes() {
        FlightRoute active      = createFakeRoute("r1", "OPO", "LIS");
        FlightRoute deactivated = createFakeRoute("r2", "OPO", "MAD");
        deactivated.deactivate("atcc_jose");

        when(routeRepository.findAll()).thenReturn(List.of(active, deactivated));

        String result = flightRouteService.exportKml();

        assertTrue(result.contains("OPO → LIS"));
        assertFalse(result.contains("OPO → MAD"));
    }

    @Test
    void ensureExportKmlReturnsEmptyDocumentWhenNoActiveRoutes() {
        when(routeRepository.findAll()).thenReturn(List.of());

        String result = flightRouteService.exportKml();

        assertTrue(result.contains("<Document>"));
        assertFalse(result.contains("<Placemark>"));
    }

    @Test
    void ensureExportGeoJsonSkipsRoutesWithNullCoordinates() {
        Airport originNoCoords = new Airport(
                new IATACode("OPO"), "Fake",
                new Location("Reg", "Country", "City", null),
                new Timezone("UTC+00:00"));

        Airport destination = createFakeAirport("LIS", null);

        RouteRequirement req = new RouteRequirement(600.0, 150);
        FlightRoute routeNoCoords = new FlightRoute("r-nocoords", originNoCoords, destination, 500.0, 60, req, "atcc_jose");

        FlightRoute validRoute = createFakeRoute("r-valid", "OPO", "MAD");

        when(routeRepository.findAll()).thenReturn(List.of(routeNoCoords, validRoute));

        String result = flightRouteService.exportGeoJson();

        assertFalse(result.contains("r-nocoords"));
        assertTrue(result.contains("r-valid"));
    }

    @Test
    void ensureExportKmlSkipsRoutesWithNullCoordinates() {
        Airport originNoCoords = new Airport(
                new IATACode("OPO"), "Fake",
                new Location("Reg", "Country", "City", null),
                new Timezone("UTC+00:00"));
        
        Airport destination = createFakeAirport("LIS", null);

        RouteRequirement req = new RouteRequirement(600.0, 150);
        FlightRoute routeNoCoords = new FlightRoute("r-nocoords", originNoCoords, destination, 500.0, 60, req, "atcc_jose");

        FlightRoute validRoute = createFakeRoute("r-valid", "OPO", "MAD");

        when(routeRepository.findAll()).thenReturn(List.of(routeNoCoords, validRoute));

        String result = flightRouteService.exportKml();

        assertFalse(result.contains("OPO → LIS"));
        assertTrue(result.contains("OPO → MAD"));
    }


    @Test
    void ensureExportGeoJsonSkipsRoutesWithNullDestinationCoordinates() {
        Airport origin = createFakeAirport("OPO", null);

        Airport destNoCoords = new Airport(
                new IATACode("LIS"), "Fake",
                new Location("Reg", "Country", "City", null),
                new Timezone("UTC+00:00"));

        RouteRequirement req = new RouteRequirement(600.0, 150);
        FlightRoute routeNoCoords = new FlightRoute("r-nocoords", origin, destNoCoords, 500.0, 60, req, "atcc_jose");

        FlightRoute validRoute = createFakeRoute("r-valid", "OPO", "MAD");

        when(routeRepository.findAll()).thenReturn(List.of(routeNoCoords, validRoute));

        String result = flightRouteService.exportGeoJson();

        assertFalse(result.contains("r-nocoords"));
        assertTrue(result.contains("r-valid"));
    }

    @Test
    void ensureExportKmlSkipsRoutesWithNullDestinationCoordinates() {
        Airport origin = createFakeAirport("OPO", null);

        Airport destNoCoords = new Airport(
                new IATACode("LIS"), "Fake",
                new Location("Reg", "Country", "City", null),
                new Timezone("UTC+00:00"));
        destNoCoords.changeStatus(null);

        RouteRequirement req = new RouteRequirement(600.0, 150);
        FlightRoute routeNoCoords = new FlightRoute("r-nocoords", origin, destNoCoords, 500.0, 60, req, "atcc_jose");

        FlightRoute validRoute = createFakeRoute("r-valid", "OPO", "MAD");

        when(routeRepository.findAll()).thenReturn(List.of(routeNoCoords, validRoute));

        String result = flightRouteService.exportKml();

        assertFalse(result.contains("OPO → LIS"));
        assertTrue(result.contains("OPO → MAD"));
    }
    @Test
    void ensureExportGeoJsonWithMultipleRoutesFormatsCorrectly() {
        FlightRoute r1 = createFakeRoute("r1", "OPO", "LIS");
        FlightRoute r2 = createFakeRoute("r2", "LIS", "MAD");

        when(routeRepository.findAll()).thenReturn(List.of(r1, r2));

        String result = flightRouteService.exportGeoJson();

        assertTrue(result.contains("\"OPO\""));
        assertTrue(result.contains("\"LIS\""));
        assertTrue(result.contains("\"MAD\""));
        assertTrue(result.endsWith("]}"));
    }
}
