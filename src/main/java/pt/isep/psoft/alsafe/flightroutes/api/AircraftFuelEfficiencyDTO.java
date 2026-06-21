package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

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