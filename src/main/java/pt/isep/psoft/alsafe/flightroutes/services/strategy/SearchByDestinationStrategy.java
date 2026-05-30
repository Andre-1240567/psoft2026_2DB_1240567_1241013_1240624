package pt.isep.psoft.alsafe.flightroutes.services.strategy;

import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

@Component
@Order(2)
public class SearchByDestinationStrategy implements RouteSearchStrategy {

    private final FlightRouteRepository repository;

    public SearchByDestinationStrategy(FlightRouteRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean supports(String originIata, String destinationIata) {
        return originIata == null && destinationIata != null;
    }

    @Override
    public Page<FlightRoute> execute(String originIata, String destinationIata, Pageable pageable) {
        return repository.findByDestination_IataCode_Code(destinationIata, pageable);
    }
}