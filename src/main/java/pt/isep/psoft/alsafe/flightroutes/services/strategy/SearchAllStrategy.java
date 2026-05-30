package pt.isep.psoft.alsafe.flightroutes.services.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

/**
 * Fallback strategy: applies when no filter parameters are provided.
 * Must be ordered last so it doesn't shadow more-specific strategies.
 */
@Component
@Order(Integer.MAX_VALUE)
public class SearchAllStrategy implements RouteSearchStrategy {

    private final FlightRouteRepository repository;

    public SearchAllStrategy(FlightRouteRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean supports(String originIata, String destinationIata) {
        // Fallback — always true, so it must be the last strategy evaluated.
        return true;
    }

    @Override
    public Page<FlightRoute> execute(String originIata, String destinationIata, Pageable pageable) {
        return repository.findAll(pageable);
    }
}