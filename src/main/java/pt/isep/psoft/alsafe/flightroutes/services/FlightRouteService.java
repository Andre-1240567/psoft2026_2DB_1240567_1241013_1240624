package pt.isep.psoft.alsafe.flightroutes.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.isep.psoft.alsafe.airportmanagement.api.dto.BusiestAirportDTO;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.Status;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;
import pt.isep.psoft.alsafe.flightroutes.api.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteModelAssembler;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.services.strategy.RouteSearchStrategy;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class FlightRouteService {

    private final FlightRouteRepository routeRepository;
    private final AirportService airportService;
    private final FlightRouteModelAssembler assembler;
    
    // Lista injetada automaticamente pelo Spring com todas as estratégias
    private final List<RouteSearchStrategy> searchStrategies;

    public FlightRouteService(FlightRouteRepository routeRepository,
                              AirportService airportService,
                              FlightRouteModelAssembler assembler,
                              List<RouteSearchStrategy> searchStrategies) {
        this.routeRepository  = routeRepository;
        this.airportService   = airportService;
        this.assembler        = assembler;
        this.searchStrategies = searchStrategies;
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
        // Garantir o Uppercase logo no início
        final String origin = originIata != null ? originIata.toUpperCase() : null;
        final String dest = destinationIata != null ? destinationIata.toUpperCase() : null;

        // O Padrão Strategy em Ação: Encontra a primeira estratégia que suporta os parâmetros
        RouteSearchStrategy activeStrategy = searchStrategies.stream()
                .filter(strategy -> strategy.supports(origin, dest))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No valid search strategy found for the provided filters."));

        // Executa a query através da estratégia encontrada
        Page<FlightRoute> resultPage = activeStrategy.execute(origin, dest, pageable);

        // Mapeia para DTO
        return resultPage.map(assembler::toModel);
    }

    @Transactional(readOnly = true)
    public Page<FlightRouteResponseDTO> getRoutesByAirport(String airportIata, Pageable pageable) {
        String iata = airportIata.toUpperCase();

        // Verify airport exists (throws ResourceNotFoundException if it doesn't)
        resolveAirport(iata);

        Page<FlightRoute> resultPage = routeRepository.findByOrigin_IataCode_CodeOrDestination_IataCode_Code(iata, iata, pageable);
        return resultPage.map(assembler::toModel);
    }

    @Transactional(readOnly = true)
    public List<BusiestAirportDTO> getBusiestAirports() {
        List<Object[]> results = routeRepository.findBusiestAirportsStatistics();
        return results.stream()
                .map(row -> new BusiestAirportDTO((String) row[0], ((Number) row[1]).longValue()))
                .toList();
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