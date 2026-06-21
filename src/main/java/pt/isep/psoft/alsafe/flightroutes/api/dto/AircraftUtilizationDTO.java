package pt.isep.psoft.alsafe.flightroutes.api.dto;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;


@Getter
@Relation(itemRelation = "utilization", collectionRelation = "utilizations")
public class AircraftUtilizationDTO extends RepresentationModel<AircraftUtilizationDTO> {

    private final String registrationNumber;
    private final String modelName;
    private final List<AircraftUtilizationPeriodDTO> utilizationByPeriod;
    private final double totalFlightHours;
    private final long totalFlights;

    public AircraftUtilizationDTO(String registrationNumber,
                                   String modelName,
                                   List<AircraftUtilizationPeriodDTO> utilizationByPeriod) {
        this.registrationNumber = registrationNumber;
        this.modelName = modelName;
        this.utilizationByPeriod = utilizationByPeriod;
        this.totalFlightHours = utilizationByPeriod.stream()
                .mapToDouble(AircraftUtilizationPeriodDTO::getTotalFlightHours)
                .sum();
        this.totalFlights = utilizationByPeriod.stream()
                .mapToLong(AircraftUtilizationPeriodDTO::getTotalFlights)
                .sum();
    }
}