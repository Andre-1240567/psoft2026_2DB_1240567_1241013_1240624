package pt.isep.psoft.alsafe.flightroutes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;

// Aqui a Entidade é FlightRoute e a chave primária também é String (routeId)
public interface FlightRouteRepository extends JpaRepository<FlightRoute, String> {
}