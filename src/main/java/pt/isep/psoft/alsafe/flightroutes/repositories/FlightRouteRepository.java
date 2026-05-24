package pt.isep.psoft.alsafe.flightroutes.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;

public interface FlightRouteRepository extends JpaRepository<FlightRoute, String> {

    // --- US113: view all routes from a specific airport (origin or destination) ---

    Page<FlightRoute> findByOrigin_IataCode_Code(String originIata, Pageable pageable);

    Page<FlightRoute> findByDestination_IataCode_Code(String destinationIata, Pageable pageable);

    // --- US114: search by origin + destination ---

    Page<FlightRoute> findByOrigin_IataCode_CodeAndDestination_IataCode_Code(
            String originIata, String destinationIata, Pageable pageable);

    // --- Status-filtered variants — prepared for Phase 2 (US214: list active routes) ---
    // Not yet wired into the search endpoint; Phase 1 search operates without status filtering.

    Page<FlightRoute> findByOrigin_IataCode_CodeAndRouteStatus(
            String originIata, RouteStatus routeStatus, Pageable pageable);

    Page<FlightRoute> findByDestination_IataCode_CodeAndRouteStatus(
            String destinationIata, RouteStatus routeStatus, Pageable pageable);

    Page<FlightRoute> findByOrigin_IataCode_CodeAndDestination_IataCode_CodeAndRouteStatus(
            String originIata, String destinationIata, RouteStatus routeStatus, Pageable pageable);

    Page<FlightRoute> findByRouteStatus(RouteStatus routeStatus, Pageable pageable);

    // --- US111: fetch route with history in a single query (avoids lazy-load outside transaction) ---

    @Query("SELECT r FROM FlightRoute r LEFT JOIN FETCH r.history WHERE r.routeId = :routeId")
    java.util.Optional<FlightRoute> findByIdWithHistory(@Param("routeId") String routeId);
}