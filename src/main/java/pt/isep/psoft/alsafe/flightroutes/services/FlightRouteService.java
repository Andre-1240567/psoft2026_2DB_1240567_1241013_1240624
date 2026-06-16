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
import pt.isep.psoft.alsafe.airportmanagement.domain.GPSCoordinates;
import pt.isep.psoft.alsafe.airportmanagement.domain.Status;
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
import pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository;
import pt.isep.psoft.alsafe.flightroutes.services.routing.AlternativeRoutingStrategy;
import pt.isep.psoft.alsafe.flightroutes.services.strategy.RouteSearchStrategy;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FlightRouteService {

    private final FlightRouteRepository routeRepository;
    private final ScheduledFlightRepository scheduledFlightRepository;
    private final AirportService airportService;
    private final FlightRouteModelAssembler assembler;
    private final List<RouteSearchStrategy> searchStrategies;
    private final List<AlternativeRoutingStrategy> routingStrategies;

    public FlightRouteService(FlightRouteRepository routeRepository,
                              ScheduledFlightRepository scheduledFlightRepository,
                              AirportService airportService,
                              FlightRouteModelAssembler assembler,
                              List<RouteSearchStrategy> searchStrategies,
                              List<AlternativeRoutingStrategy> routingStrategies) {
        this.routeRepository           = routeRepository;
        this.scheduledFlightRepository = scheduledFlightRepository;
        this.airportService            = airportService;
        this.assembler                 = assembler;
        this.searchStrategies          = searchStrategies;
        this.routingStrategies         = routingStrategies;
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
        final String dest   = destinationIata != null ? destinationIata.toUpperCase() : null;

        RouteSearchStrategy activeStrategy = searchStrategies.stream()
                .filter(strategy -> strategy.supports(origin, dest))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No valid search strategy found for the provided filters."));

        return activeStrategy.execute(origin, dest, pageable).map(assembler::toModel);
    }

    @Transactional(readOnly = true)
    public Page<FlightRouteResponseDTO> getActiveRoutesSorted(RouteStatus status, String sortBy, Pageable pageable) {
        if ("popularity".equalsIgnoreCase(sortBy)) {
            return routeRepository.findActiveRoutesByPopularity(status, pageable).map(assembler::toModel);
            
        } else if ("distance".equalsIgnoreCase(sortBy)) {
            Pageable sortedByDistance = org.springframework.data.domain.PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    org.springframework.data.domain.Sort.by("distance").ascending()
            );
            return routeRepository.findByRouteStatus(status, sortedByDistance).map(assembler::toModel);
            
        } else {
            throw new IllegalArgumentException("Invalid sort parameter. Use 'popularity' or 'distance'.");
        }
    }

    @Transactional(readOnly = true)
    public Double getTotalNetworkDistance() {
        return routeRepository.calculateTotalNetworkDistance(RouteStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public List<AlternativeRouteResponseDTO> findAlternativeRoutes(
            String originIata, String destinationIata, String algorithm) {

        resolveAirport(originIata.toUpperCase());
        resolveAirport(destinationIata.toUpperCase());

        AlternativeRoutingStrategy strategyToUse = routingStrategies.stream()
                .filter(s -> s.getAlgorithmName().equalsIgnoreCase(algorithm))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Routing algorithm not supported: " + algorithm));

        List<List<FlightRoute>> paths = strategyToUse.findAlternatives(
                originIata.toUpperCase(), destinationIata.toUpperCase());

        return paths.stream().map(path -> {
            List<FlightRouteResponseDTO> legs = path.stream().map(assembler::toModel).toList();
            Double totalDistance = path.stream().mapToDouble(FlightRoute::getDistance).sum();
            Integer totalTime    = path.stream().mapToInt(FlightRoute::getEstimatedFlightTime).sum();
            return new AlternativeRouteResponseDTO(legs, totalDistance, totalTime);
        }).toList();
    }

    @Transactional(readOnly = true)
    public Page<FlightRouteResponseDTO> getRoutesByAirport(String airportIata, Pageable pageable) {
        String iata = airportIata.toUpperCase();
        resolveAirport(iata);
        return routeRepository
                .findByOrigin_IataCode_CodeOrDestination_IataCode_Code(iata, iata, pageable)
                .map(assembler::toModel);
    }

    @Transactional(readOnly = true)
    public List<FlightRouteResponseDTO> getCompatibleRoutesForAircraft(Double maxRange, Integer capacity) {
        return routeRepository.findCompatibleRoutes(maxRange, capacity)
                .stream().map(assembler::toModel).toList();
    }

    @Transactional(readOnly = true)
    public List<BusiestAirportDTO> getBusiestAirports() {
        return routeRepository.findBusiestAirportsStatistics().stream()
                .map(row -> new BusiestAirportDTO((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    // -----------------------------------------------------------------------
    // Bonus US229 — Flight utilization report
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<RouteUtilizationDTO> getRouteUtilizationReport() {
        return scheduledFlightRepository.findRouteUtilizationReport().stream()
                .map(row -> new RouteUtilizationDTO(
                        (String) row[0],
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public String exportGeoJson() {
        List<FlightRoute> routes = routeRepository.findAll().stream()
            .filter(r -> r.getRouteStatus() == RouteStatus.ACTIVE)
            .filter(r -> r.getOrigin().getLocation().getCoordinates() != null
                    && r.getDestination().getLocation().getCoordinates() != null)
            .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"FeatureCollection\",\"features\":[");

        for (int i = 0; i < routes.size(); i++) {
            FlightRoute r = routes.get(i);
            GPSCoordinates origin = r.getOrigin().getLocation().getCoordinates();
            GPSCoordinates dest   = r.getDestination().getLocation().getCoordinates();

            sb.append("{\"type\":\"Feature\",\"properties\":{")
            .append("\"routeId\":\"").append(r.getRouteIdValue()).append("\",")
            .append("\"origin\":\"").append(r.getOrigin().getIataCode().getCode()).append("\",")
            .append("\"destination\":\"").append(r.getDestination().getIataCode().getCode()).append("\",")
            .append("\"distance\":").append(r.getDistance()).append(",")
            .append("\"status\":\"").append(r.getRouteStatus()).append("\"")
            .append("},\"geometry\":{\"type\":\"LineString\",\"coordinates\":[")
            .append("[").append(origin.getLongitude()).append(",").append(origin.getLatitude()).append("],")
            .append("[").append(dest.getLongitude()).append(",").append(dest.getLatitude()).append("]")
            .append("]}}");

            if (i < routes.size() - 1) sb.append(",");
        }

        sb.append("]}");
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String exportKml() {
        List<FlightRoute> routes = routeRepository.findAll().stream()
            .filter(r -> r.getRouteStatus() == RouteStatus.ACTIVE)
            .filter(r -> r.getOrigin().getLocation().getCoordinates() != null
                    && r.getDestination().getLocation().getCoordinates() != null)
            .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
        .append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">")
        .append("<Document><name>AISafe Route Network</name>");

        for (FlightRoute r : routes) {
            GPSCoordinates origin = r.getOrigin().getLocation().getCoordinates();
            GPSCoordinates dest   = r.getDestination().getLocation().getCoordinates();

            sb.append("<Placemark>")
            .append("<name>").append(r.getOrigin().getIataCode().getCode())
            .append(" → ").append(r.getDestination().getIataCode().getCode()).append("</name>")
            .append("<description>Distance: ").append(r.getDistance()).append(" km</description>")
            .append("<LineString><coordinates>")
            .append(origin.getLongitude()).append(",").append(origin.getLatitude()).append(",0 ")
            .append(dest.getLongitude()).append(",").append(dest.getLatitude()).append(",0")
            .append("</coordinates></LineString>")
            .append("</Placemark>");
        }

        sb.append("</Document></kml>");
        return sb.toString();
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