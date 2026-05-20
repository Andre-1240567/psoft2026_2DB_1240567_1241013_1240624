package pt.isep.psoft.alsafe.flightroutes.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;

public interface FlightRouteRepository extends JpaRepository<FlightRoute, String> {
    
    Page<FlightRoute> findByOrigin_IataCode_Code(String originIata, Pageable pageable);
    
    Page<FlightRoute> findByDestination_IataCode_Code(String destinationIata, Pageable pageable);
    
    Page<FlightRoute> findByOrigin_IataCode_CodeAndDestination_IataCode_Code(String originIata, String destinationIata, Pageable pageable);
}