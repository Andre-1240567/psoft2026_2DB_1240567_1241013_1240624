package pt.isep.psoft.alsafe.flightroutes.services;

import org.springframework.stereotype.Service;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;

import pt.isep.psoft.alsafe.flightroutes.api.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class FlightRouteService {

    private final FlightRouteRepository routeRepository;
    private final AirportRepository airportRepository;

    public FlightRouteService(FlightRouteRepository routeRepository, AirportRepository airportRepository) {
        this.routeRepository = routeRepository;
        this.airportRepository = airportRepository;
    }

    public FlightRoute createFlightRoute(CreateFlightRouteDTO dto) {
        
        Airport origin = airportRepository.findByIataCode_Code(dto.getOriginIata())
                .orElseThrow(() -> new IllegalArgumentException("Origin airport not found: " + dto.getOriginIata()));

        Airport destination = airportRepository.findByIataCode_Code(dto.getDestinationIata())
                .orElseThrow(() -> new IllegalArgumentException("Destination airport not found: " + dto.getDestinationIata()));

        RouteRequirement requirements = new RouteRequirement(dto.getMinRangeRequired(), dto.getMinCapacityRequired());

        String routeId = UUID.randomUUID().toString(); 

        FlightRoute route = new FlightRoute(routeId, origin, destination, 
                                        dto.getDistance(), dto.getEstimatedFlightTime(), 
                                        requirements, getCurrentUser());

        return routeRepository.save(route);
    }

    public FlightRoute deactivateRoute(String routeId) {
        FlightRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Flight Route not found: " + routeId));

        route.deactivate(getCurrentUser());

        return routeRepository.save(route);
    }

    public FlightRoute updateRoute(String routeId, UpdateFlightRouteDTO dto) {
        FlightRoute route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Flight Route not found: " + routeId));

        if (dto.getVersion() == null || !route.getVersion().equals(dto.getVersion())) {
            throw new IllegalStateException("Conflito de Concorrência: A rota foi alterada por outro utilizador entretanto. Atualize a página e tente novamente.");
        }

        RouteRequirement newRequirements = new RouteRequirement(dto.getMinRangeRequired(), dto.getMinCapacityRequired());

        route.updateDetails(dto.getDistance(), dto.getEstimatedFlightTime(), newRequirements, getCurrentUser());

        return routeRepository.save(route);
    }

    public FlightRoute getRouteById(String routeId) {
        return routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Flight Route not found: " + routeId));
    }

    public Page<FlightRoute> searchRoutes(String originIata, String destinationIata, Pageable pageable) {
        if (originIata != null) originIata = originIata.toUpperCase();
        if (originIata != null && destinationIata != null) {
            return routeRepository.findByOrigin_IataCode_CodeAndDestination_IataCode_Code(originIata, destinationIata, pageable);
        } else if (originIata != null) {
            return routeRepository.findByOrigin_IataCode_Code(originIata, pageable);
        } else if (destinationIata != null) {
            return routeRepository.findByDestination_IataCode_Code(destinationIata, pageable);
        } else {
            return routeRepository.findAll(pageable); 
        }
    }

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            return auth.getName(); 
        }
        return "System"; 
    }
}