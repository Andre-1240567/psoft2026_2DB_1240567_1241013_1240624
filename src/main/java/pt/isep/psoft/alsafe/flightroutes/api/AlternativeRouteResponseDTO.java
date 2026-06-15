package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class AlternativeRouteResponseDTO extends RepresentationModel<AlternativeRouteResponseDTO> {

    private final List<FlightRouteResponseDTO> routeLegs;
    private final Double totalDistance;
    private final Integer totalEstimatedFlightTime;
    private final Integer numberOfStops;

    public AlternativeRouteResponseDTO(List<FlightRouteResponseDTO> routeLegs, 
                                       Double totalDistance, 
                                       Integer totalEstimatedFlightTime) {
        this.routeLegs = routeLegs;
        this.totalDistance = totalDistance;
        this.totalEstimatedFlightTime = totalEstimatedFlightTime;
        this.numberOfStops = Math.max(0, routeLegs.size() - 1); 
    }
}