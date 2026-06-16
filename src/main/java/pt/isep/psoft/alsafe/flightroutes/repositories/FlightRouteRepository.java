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

import java.util.List;
import java.util.Optional;

public interface FlightRouteRepository extends JpaRepository<FlightRoute, RouteId> {

    @Query("SELECT r FROM FlightRoute r WHERE r.routeId.id = :id")
    Optional<FlightRoute> findById(@Param("id") String id);

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByOrigin_IataCode_Code(String originIata, Pageable pageable);

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByDestination_IataCode_Code(String destinationIata, Pageable pageable);

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByOrigin_IataCode_CodeOrDestination_IataCode_Code(
            String originIata, String destinationIata, Pageable pageable);

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findByOrigin_IataCode_CodeAndDestination_IataCode_Code(
            String originIata, String destinationIata, Pageable pageable);

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

    @EntityGraph(attributePaths = {"history"})
    Page<FlightRoute> findAll(Pageable pageable);

    @Query("SELECT r FROM FlightRoute r WHERE r.routeStatus = 'ACTIVE' AND r.routeRequirement.minRangeRequired <= :maxRange AND r.routeRequirement.minCapacityRequired <= :capacity")
    List<FlightRoute> findCompatibleRoutes(@Param("maxRange") Double maxRange, @Param("capacity") Integer capacity);

    @Query("SELECT r FROM FlightRoute r LEFT JOIN FETCH r.history WHERE r.routeId.id = :routeId")
    Optional<FlightRoute> findByIdWithHistory(@Param("routeId") String routeId);

    @Query(value = "SELECT r FROM FlightRoute r " +
                   "LEFT JOIN ScheduledFlight sf ON sf.route = r " +
                   "WHERE r.routeStatus = :status " +
                   "GROUP BY r " +
                   "ORDER BY COUNT(sf) DESC",
           countQuery = "SELECT count(r) FROM FlightRoute r WHERE r.routeStatus = :status")
    Page<FlightRoute> findActiveRoutesByPopularity(
            @Param("status") RouteStatus status,
            Pageable pageable);

    @Query("SELECT COALESCE(SUM(r.distance), 0.0) FROM FlightRoute r WHERE r.routeStatus = :status")
    Double calculateTotalNetworkDistance(@Param("status") RouteStatus status);

    @Query(value = "SELECT a.iata_code as iata, COUNT(*) as routeCount " +
                   "FROM airport a " +
                   "JOIN ( " +
                   "  SELECT origin_id AS airport_id FROM flight_route " +
                   "  UNION ALL " +
                   "  SELECT destination_id AS airport_id FROM flight_route " +
                   ") r ON a.id = r.airport_id " +
                   "GROUP BY a.iata_code " +
                   "ORDER BY routeCount DESC",
            nativeQuery = true)
        List<Object[]> findBusiestAirportsStatistics();

    @Query("SELECT r FROM FlightRoute r WHERE r.routeStatus = 'ACTIVE' AND r.origin.iataCode.code = :originIata")
        List<FlightRoute> findActiveRoutesByOrigin(@Param("originIata") String originIata);
}