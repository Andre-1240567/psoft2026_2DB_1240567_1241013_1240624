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
import pt.isep.psoft.alsafe.flightroutes.api.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public class FlightRouteService {

    private final FlightRouteRepository routeRepository;
    private final AirportService airportService; // Utilizing DDD by invoking colleague's Application Service

    public FlightRouteService(FlightRouteRepository routeRepository, AirportService airportService) {
        this.routeRepository = routeRepository;
        this.airportService = airportService;
    }

    @Transactional
    public FlightRouteResponseDTO createFlightRoute(CreateFlightRouteDTO dto) {

        String originIata = dto.getOriginIata().toUpperCase();
        String destinationIata = dto.getDestinationIata().toUpperCase();

        // airportService.getAirportDetails throws IllegalArgumentException if missing, 
        // which the global handler perfectly captures as a 400 Bad Request for bad input data.
        Airport origin = airportService.getAirportDetails(originIata);
        Airport destination = airportService.getAirportDetails(destinationIata);

        if (origin.getStatus() == null || origin.getStatus() != Status.OPERATIONAL) {
            throw new IllegalArgumentException("Origin airport '" + originIata + "' is not operational and cannot be used in a flight route.");
        }
        if (destination.getStatus() == null || destination.getStatus() != Status.OPERATIONAL) {
            throw new IllegalArgumentException("Destination airport '" + destinationIata + "' is not operational and cannot be used in a flight route.");
        }

        RouteRequirement requirements = new RouteRequirement(dto.getMinRangeRequired(), dto.getMinCapacityRequired());
        FlightRoute route = new FlightRoute(UUID.randomUUID().toString(), origin, destination,
                dto.getDistance(), dto.getEstimatedFlightTime(),
                requirements, getCurrentUser());

        return new FlightRouteResponseDTO(routeRepository.save(route));
    }

    @Transactional
    public FlightRouteResponseDTO deactivateRoute(String routeId) {
        FlightRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight route not found: " + routeId));

        route.deactivate(getCurrentUser());
        return new FlightRouteResponseDTO(routeRepository.save(route));
    }

    @Transactional
    public FlightRouteResponseDTO updateRoute(String routeId, UpdateFlightRouteDTO dto) {
        FlightRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight route not found: " + routeId));

        // Let Spring's mapped exception handle the 409 conflict
        if (dto.getVersion() == null || !route.getVersion().equals(dto.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(FlightRoute.class, routeId);
        }

        RouteRequirement newRequirements = new RouteRequirement(dto.getMinRangeRequired(), dto.getMinCapacityRequired());
        route.updateDetails(dto.getDistance(), dto.getEstimatedFlightTime(), newRequirements, getCurrentUser());

        return new FlightRouteResponseDTO(routeRepository.save(route));
    }

    @Transactional(readOnly = true)
    public FlightRouteResponseDTO getRouteById(String routeId) {
        FlightRoute route = routeRepository.findByIdWithHistory(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight route not found: " + routeId));
        return new FlightRouteResponseDTO(route);
    }

    @Transactional(readOnly = true)
    public List<FlightRouteResponseDTO.RouteHistoryDTO> getRouteHistory(String routeId) {
        FlightRoute route = routeRepository.findByIdWithHistory(routeId) // Using optimized fetch query
                .orElseThrow(() -> new ResourceNotFoundException("Flight route not found: " + routeId));
        
        return route.getHistory().stream()
                .map(FlightRouteResponseDTO.RouteHistoryDTO::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<FlightRouteResponseDTO> searchRoutes(String originIata, String destinationIata, Pageable pageable) {
        if (originIata != null) originIata = originIata.toUpperCase();
        if (destinationIata != null) destinationIata = destinationIata.toUpperCase();

        Page<FlightRoute> resultPage;

        if (originIata != null && destinationIata != null) {
            resultPage = routeRepository.findByOrigin_IataCode_CodeAndDestination_IataCode_Code(originIata, destinationIata, pageable);
        } else if (originIata != null) {
            resultPage = routeRepository.findByOrigin_IataCode_Code(originIata, pageable);
        } else if (destinationIata != null) {
            resultPage = routeRepository.findByDestination_IataCode_Code(destinationIata, pageable);
        } else {
            resultPage = routeRepository.findAll(pageable);
        }
        
        // This safe mapping triggers lazy collections within the open Transaction
        return resultPage.map(FlightRouteResponseDTO::new);
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName();
        }
        return "System";
    }
}