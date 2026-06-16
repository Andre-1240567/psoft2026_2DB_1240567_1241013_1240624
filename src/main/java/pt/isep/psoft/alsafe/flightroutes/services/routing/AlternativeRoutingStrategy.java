package pt.isep.psoft.alsafe.flightroutes.services.routing;

import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import java.util.List;

public interface AlternativeRoutingStrategy {
    
    List<List<FlightRoute>> findAlternatives(String originIata, String destinationIata);
    
    String getAlgorithmName();
}