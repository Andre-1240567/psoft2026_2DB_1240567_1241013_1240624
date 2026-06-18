package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * DTO for US227 - Fuel efficiency metrics per route.
 *
 * Fuel consumption per flight on this route is estimated using the minimum
 * aircraft requirement (minRangeRequired) as a proxy for the model that would
 * typically operate it, combined with the route's actual distance.
 *
 * Per-route efficiency is route-distance-based and model-agnostic:
 *   estimatedFuelPerFlight = fuelBurnRate × route.distance
 * where fuelBurnRate comes from the model of each aircraft that flew the route.
 * For the route-level view (no specific aircraft), we report the raw distance
 * and leave fuel estimation to the per-aircraft endpoints.
 */
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