package pt.isep.psoft.alsafe.flightroutes.services.strategy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;

public interface RouteSearchStrategy {

    boolean supports(String originIata, String destinationIata);

    Page<FlightRoute> execute(String originIata, String destinationIata, Pageable pageable);
}