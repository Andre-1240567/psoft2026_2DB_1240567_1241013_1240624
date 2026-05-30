package pt.isep.psoft.alsafe.flightroutes.services.strategy;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;

/**
 * Strategy interface for searching flight routes.
 * Each implementation handles a specific combination of filter parameters.
 *
 * New search variants (e.g. filter by status) can be added by creating a new
 * @Component that implements this interface — no changes to FlightRouteService needed.
 */
public interface RouteSearchStrategy {

    /** Returns true if this strategy applies to the given filter combination. */
    boolean supports(String originIata, String destinationIata);

    /** Executes the search and returns a paginated result. */
    Page<FlightRoute> execute(String originIata, String destinationIata, Pageable pageable);
}