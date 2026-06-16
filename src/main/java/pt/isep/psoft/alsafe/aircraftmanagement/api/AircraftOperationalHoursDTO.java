package pt.isep.psoft.alsafe.aircraftmanagement.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;

@Data
@EqualsAndHashCode(callSuper = false)
public class AircraftOperationalHoursDTO extends RepresentationModel<AircraftOperationalHoursDTO> {

    private String registrationNumber;
    private String modelName;
    private String status;
    private Double totalOperationalHours;

    public AircraftOperationalHoursDTO(Aircraft aircraft) {
        this.registrationNumber = aircraft.getRegistrationNumber();
        this.modelName = aircraft.getModel().getModelName();
        this.status = aircraft.getStatus().name();
        this.totalOperationalHours = aircraft.getTotalFlightHours();
    }
}
