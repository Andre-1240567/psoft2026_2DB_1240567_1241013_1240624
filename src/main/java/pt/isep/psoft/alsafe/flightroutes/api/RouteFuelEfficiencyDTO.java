package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
public class RouteFuelEfficiencyDTO extends RepresentationModel<RouteFuelEfficiencyDTO> {

    private final String routeId;
    private final String originIata;
    private final String destinationIata;

    private final double distanceKm;

    private final double estimatedFuelPerFlightL;

    private final double efficiencyKmPerL;

    private final int flightCount;

    public RouteFuelEfficiencyDTO(String routeId,
                                  String originIata,
                                  String destinationIata,
                                  double distanceKm,
                                  double estimatedFuelPerFlightL,
                                  double efficiencyKmPerL,
                                  int flightCount) {
        this.routeId                  = routeId;
        this.originIata               = originIata;
        this.destinationIata          = destinationIata;
        this.distanceKm               = distanceKm;
        this.estimatedFuelPerFlightL  = estimatedFuelPerFlightL;
        this.efficiencyKmPerL         = efficiencyKmPerL;
        this.flightCount              = flightCount;
    }
}