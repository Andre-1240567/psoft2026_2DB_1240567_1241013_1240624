package pt.isep.psoft.alsafe.aircraftmanagement.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;

@Data
@EqualsAndHashCode(callSuper = false)
public class AircraftResponseDTO extends RepresentationModel<AircraftResponseDTO> {

    private String registrationNumber;
    private String modelName;
    private java.time.LocalDate manufacturingDate;
    private String activeConfigurationName;
    private Integer activeCapacity;
    private String status;
    private Long version;

    public AircraftResponseDTO(Aircraft aircraft) {
        this.registrationNumber = aircraft.getRegistrationNumber();
        this.modelName = aircraft.getModel().getModelName();
        this.manufacturingDate = aircraft.getManufacturingDate();
        this.activeConfigurationName = aircraft.getActiveConfigurationName();
        this.activeCapacity = aircraft.getActiveCapacity();
        this.status = aircraft.getStatus().name();
        this.version = aircraft.getVersion();
    }
}
