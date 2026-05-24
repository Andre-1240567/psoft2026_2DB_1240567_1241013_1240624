package pt.isep.psoft.alsafe.flightroutes.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteId;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;

import java.util.Optional;

public interface FlightRouteRepository extends JpaRepository<FlightRoute, RouteId> {

    // --- Convenience finder by raw String ID (avoids constructing RouteId at call sites) ---

    @Query("SELECT r FROM FlightRoute r WHERE r.routeId.id = :id")
    Optional<FlightRoute> findById(@Param("id") String id);

    // --- US113: view all routes from a specific airport (origin or destination) ---

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByOrigin_IataCode_Code(String originIata, Pageable pageable);

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByDestination_IataCode_Code(String destinationIata, Pageable pageable);

    // --- US114: search by origin + destination ---

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByOrigin_IataCode_CodeAndDestination_IataCode_Code(
            String originIata, String destinationIata, Pageable pageable);

    // --- Status-filtered variants — prepared for Phase 2 (US214: list active routes) ---

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByOrigin_IataCode_CodeAndRouteStatus(
            String originIata, RouteStatus routeStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByDestination_IataCode_CodeAndRouteStatus(
            String destinationIata, RouteStatus routeStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByOrigin_IataCode_CodeAndDestination_IataCode_CodeAndRouteStatus(
            String originIata, String destinationIata, RouteStatus routeStatus, Pageable pageable);

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByRouteStatus(RouteStatus routeStatus, Pageable pageable);

    // Override findAll to also fetch history eagerly during pagination
    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findAll(Pageable pageable);

    // --- US111: fetch route with history in a single query ---

    @Query("SELECT r FROM FlightRoute r LEFT JOIN FETCH r.history WHERE r.routeId.id = :routeId")
    Optional<FlightRoute> findByIdWithHistory(@Param("routeId") String routeId);
}