package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

/**
 * DTO for US227 - Fuel efficiency metrics per aircraft.
 *
 * Design decision: fuelBurnRate is derived from AircraftModel.fuelCapacity / AircraftModel.maxRange,
 * representing average fuel consumption (L/km) over the model's certified maximum range.
 * This is an approximation — the system does not store a dedicated fuelBurnPerKm field.
 * A future enhancement could add that field to AircraftModel for higher accuracy.
 */
@Getter
public class AircraftFuelEfficiencyDTO extends RepresentationModel<AircraftFuelEfficiencyDTO> {

    private final String registrationNumber;
    private final String modelName;

    private final double fuelBurnRateLPerKm;

    private final double totalDistanceFlownKm;

    private final double totalEstimatedFuelL;

    private final double efficiencyKmPerL;

    private final int flightCount;

    public AircraftFuelEfficiencyDTO(String registrationNumber,
                                     String modelName,
                                     double fuelBurnRateLPerKm,
                                     double totalDistanceFlownKm,
                                     double totalEstimatedFuelL,
                                     double efficiencyKmPerL,
                                     int flightCount) {
        this.registrationNumber   = registrationNumber;
        this.modelName            = modelName;
        this.fuelBurnRateLPerKm   = fuelBurnRateLPerKm;
        this.totalDistanceFlownKm = totalDistanceFlownKm;
        this.totalEstimatedFuelL  = totalEstimatedFuelL;
        this.efficiencyKmPerL     = efficiencyKmPerL;
        this.flightCount          = flightCount;
    }
}