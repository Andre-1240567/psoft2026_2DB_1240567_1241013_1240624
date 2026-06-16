package pt.isep.psoft.alsafe.flightroutes.services.routing;

import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import java.util.*;

@Component
public class EcoFriendlyRoutingStrategy implements AlternativeRoutingStrategy {

    private final FlightRouteRepository routeRepository;

    public EcoFriendlyRoutingStrategy(FlightRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public String getAlgorithmName() {
        return "eco-friendly";
    }

    @Override
    public List<List<FlightRoute>> findAlternatives(String originIata, String destinationIata) {
        List<FlightRoute> activeRoutes = routeRepository.findAll().stream()
                .filter(r -> r.getRouteStatus() == RouteStatus.ACTIVE)
                .toList();

        List<List<FlightRoute>> validPaths = new ArrayList<>();
        Set<String> visitedAirports = new HashSet<>();
        visitedAirports.add(originIata.toUpperCase());

        findPathsDFS(originIata.toUpperCase(), destinationIata.toUpperCase(), activeRoutes, visitedAirports, new ArrayList<>(), validPaths, 4);

        validPaths.sort(Comparator.comparingDouble(this::calculateTotalDistance));

        return validPaths.stream()
                .limit(3)
                .toList();
    }

    private void findPathsDFS(String current, String destination, List<FlightRoute> allRoutes,
                              Set<String> visited, List<FlightRoute> currentPath,
                              List<List<FlightRoute>> validPaths, int depthLimit) {
        
        if (current.equals(destination)) {
            validPaths.add(new ArrayList<>(currentPath));
            return;
        }
        if (currentPath.size() >= depthLimit) {
            return;
        }

        for (FlightRoute route : allRoutes) {
            if (route.getOrigin().getIataCode().getCode().equals(current)) {
                String nextAirport = route.getDestination().getIataCode().getCode();
                
                if (!visited.contains(nextAirport)) {
                    visited.add(nextAirport);
                    currentPath.add(route);

                    findPathsDFS(nextAirport, destination, allRoutes, visited, currentPath, validPaths, depthLimit);

                    currentPath.remove(currentPath.size() - 1);
                    visited.remove(nextAirport);
                }
            }
        }
    }

    private double calculateTotalDistance(List<FlightRoute> path) {
        return path.stream().mapToDouble(FlightRoute::getDistance).sum();
    }
}