package pt.isep.psoft.alsafe.flightroutes.services.routing;

import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import java.util.*;

@Component
public class FewestStopsRoutingStrategy implements AlternativeRoutingStrategy {

    private final FlightRouteRepository routeRepository;

    public FewestStopsRoutingStrategy(FlightRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public String getAlgorithmName() {
        return "fewest-stops";
    }

    @Override
    public List<List<FlightRoute>> findAlternatives(String originIata, String destinationIata) {
        List<FlightRoute> allActiveRoutes = routeRepository.findAll().stream()
                .filter(r -> r.getRouteStatus() == RouteStatus.ACTIVE)
                .toList();

        List<List<FlightRoute>> validPaths = new ArrayList<>();
        Queue<List<FlightRoute>> queue = new LinkedList<>();
        
        for (FlightRoute route : allActiveRoutes) {
            if (route.getOrigin().getIataCode().getCode().equalsIgnoreCase(originIata)) {
                List<FlightRoute> initialPath = new ArrayList<>();
                initialPath.add(route);
                queue.add(initialPath);
            }
        }

        int maxDepth = 3;

        while (!queue.isEmpty()) {
            List<FlightRoute> currentPath = queue.poll();
            FlightRoute lastRouteInPath = currentPath.get(currentPath.size() - 1);
            String currentAirport = lastRouteInPath.getDestination().getIataCode().getCode();

            if (currentAirport.equalsIgnoreCase(destinationIata)) {
                validPaths.add(currentPath);
                continue; 
            }

            if (currentPath.size() < maxDepth) {
                for (FlightRoute nextRoute : allActiveRoutes) {
                    if (nextRoute.getOrigin().getIataCode().getCode().equalsIgnoreCase(currentAirport)) {
                        boolean createsCycle = currentPath.stream()
                                .anyMatch(r -> r.getOrigin().getIataCode().getCode().equals(nextRoute.getDestination().getIataCode().getCode()));
                        
                        if (!createsCycle) {
                            List<FlightRoute> newPath = new ArrayList<>(currentPath);
                            newPath.add(nextRoute);
                            queue.add(newPath);
                        }
                    }
                }
            }
        }

        validPaths.sort(Comparator.comparingInt(List::size));
        
        return validPaths;
    }
}