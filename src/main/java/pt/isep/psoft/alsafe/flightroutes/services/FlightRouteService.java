package pt.isep.psoft.alsafe.flightroutes.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.Status;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;
import pt.isep.psoft.alsafe.flightroutes.api.AlternativeRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteModelAssembler;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.services.routing.AlternativeRoutingStrategy;
import pt.isep.psoft.alsafe.flightroutes.services.strategy.RouteSearchStrategy;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class FlightRouteService {

    private final FlightRouteRepository routeRepository;
    private final AirportService airportService;
    private final FlightRouteModelAssembler assembler;
    
    // Estratégias para a US114 (Procura Direta)
    private final List<RouteSearchStrategy> searchStrategies;
    
    // Estratégias para a US216 (Rotas Alternativas com Escalas)
    private final List<AlternativeRoutingStrategy> routingStrategies;

    public FlightRouteService(FlightRouteRepository routeRepository,
                              AirportService airportService,
                              FlightRouteModelAssembler assembler,
                              List<RouteSearchStrategy> searchStrategies,
                              List<AlternativeRoutingStrategy> routingStrategies) {
        this.routeRepository   = routeRepository;
        this.airportService    = airportService;
        this.assembler         = assembler;
        this.searchStrategies  = searchStrategies;
        this.routingStrategies = routingStrategies;
    }

    @Transactional
    public FlightRouteResponseDTO createFlightRoute(CreateFlightRouteDTO dto) {

        String originIata      = dto.getOriginIata().toUpperCase();
        String destinationIata = dto.getDestinationIata().toUpperCase();

        Airport origin      = resolveAirport(originIata);
        Airport destination = resolveAirport(destinationIata);

        if (origin.getStatus() == null || origin.getStatus() != Status.OPERATIONAL) {
            throw new IllegalArgumentException(
                    "Origin airport '" + originIata + "' is not operational and cannot be used in a flight route.");
        }
        if (destination.getStatus() == null || destination.getStatus() != Status.OPERATIONAL) {
            throw new IllegalArgumentException(
                    "Destination airport '" + destinationIata + "' is not operational and cannot be used in a flight route.");
        }

        RouteRequirement requirements =
                new RouteRequirement(dto.getMinRangeRequired(), dto.getMinCapacityRequired());

        FlightRoute route = new FlightRoute(
                UUID.randomUUID().toString(), origin, destination,
                dto.getDistance(), dto.getEstimatedFlightTime(),
                requirements, getCurrentUser());

        return assembler.toModel(routeRepository.save(route));
    }

    @Transactional
    public FlightRouteResponseDTO deactivateRoute(String routeId) {
        FlightRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight route not found: " + routeId));

        route.deactivate(getCurrentUser());
        return assembler.toModel(routeRepository.save(route));
    }

    @Transactional
    public FlightRouteResponseDTO updateRoute(String routeId, UpdateFlightRouteDTO dto) {
        FlightRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight route not found: " + routeId));

        if (dto.getVersion() == null || !route.getVersion().equals(dto.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(FlightRoute.class, routeId);
        }

        RouteRequirement newRequirements =
                new RouteRequirement(dto.getMinRangeRequired(), dto.getMinCapacityRequired());
        route.updateDetails(dto.getDistance(), dto.getEstimatedFlightTime(),
                            newRequirements, getCurrentUser());

        return assembler.toModel(routeRepository.save(route));
    }

    @Transactional(readOnly = true)
    public FlightRouteResponseDTO getRouteById(String routeId) {
        FlightRoute route = routeRepository.findByIdWithHistory(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight route not found: " + routeId));
        return assembler.toModel(route);
    }

    @Transactional(readOnly = true)
    public List<FlightRouteResponseDTO.RouteHistoryDTO> getRouteHistory(String routeId) {
        FlightRoute route = routeRepository.findByIdWithHistory(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight route not found: " + routeId));

        return route.getHistory().stream()
                .map(FlightRouteResponseDTO.RouteHistoryDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<FlightRouteResponseDTO> searchRoutes(String originIata, String destinationIata, Pageable pageable) {
        final String origin = originIata != null ? originIata.toUpperCase() : null;
        final String dest = destinationIata != null ? destinationIata.toUpperCase() : null;

        RouteSearchStrategy activeStrategy = searchStrategies.stream()
                .filter(strategy -> strategy.supports(origin, dest))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No valid search strategy found for the provided filters."));

        Page<FlightRoute> resultPage = activeStrategy.execute(origin, dest, pageable);

        return resultPage.map(assembler::toModel);
    }

    @Transactional(readOnly = true)
    public Page<FlightRouteResponseDTO> getActiveRoutesSorted(RouteStatus status, String sortBy, Pageable pageable) {
        String validSortBy = "distance";
        if ("popularity".equalsIgnoreCase(sortBy)) {
            validSortBy = "popularity";
        } else if ("distance".equalsIgnoreCase(sortBy)) {
            validSortBy = "distance";
        } else {
             throw new IllegalArgumentException("Invalid sort parameter. Use 'popularity' or 'distance'.");
        }

        Page<FlightRoute> resultPage = routeRepository.findActiveRoutesSorted(status, validSortBy, pageable);

        return resultPage.map(assembler::toModel);
    }

    @Transactional(readOnly = true)
    public Double getTotalNetworkDistance() {
        return routeRepository.calculateTotalNetworkDistance(RouteStatus.ACTIVE);
    }

    // ---------------------------------------------------------------------------
    // US216: Rotas Alternativas
    // ---------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<AlternativeRouteResponseDTO> findAlternativeRoutes(
            String originIata, String destinationIata, String algorithm) {
        
        // Verifica se os aeroportos existem (garante fail-fast)
        resolveAirport(originIata.toUpperCase());
        resolveAirport(destinationIata.toUpperCase());

        // Seleciona a estratégia dinâmica injetada pelo Spring
        AlternativeRoutingStrategy strategyToUse = routingStrategies.stream()
                .filter(s -> s.getAlgorithmName().equalsIgnoreCase(algorithm))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Routing algorithm not supported: " + algorithm));

        // Executa o algoritmo de pesquisa (ex: fewest-stops)
        List<List<FlightRoute>> paths = strategyToUse.findAlternatives(originIata.toUpperCase(), destinationIata.toUpperCase());

        // Transforma o resultado de domínio para o DTO consolidado
        return paths.stream().map(path -> {
            List<FlightRouteResponseDTO> legs = path.stream().map(assembler::toModel).toList();
            Double totalDistance = path.stream().mapToDouble(FlightRoute::getDistance).sum();
            Integer totalTime = path.stream().mapToInt(FlightRoute::getEstimatedFlightTime).sum();
            
            return new AlternativeRouteResponseDTO(legs, totalDistance, totalTime);
        }).toList();
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private Airport resolveAirport(String iata) {
        try {
            return airportService.getAirportDetails(iata);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Airport with IATA code '" + iata + "' not found.");
        }
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "System";
    }
}