package pt.isep.psoft.alsafe.aircraftmanagement.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class AircraftResponseDTO {

    private String registrationNumber;
    private String modelName;
    private LocalDate manufacturingDate;
    private String activeConfigurationName;
    private String status;
    private Long version;

    public AircraftResponseDTO(Aircraft aircraft) {
        this.registrationNumber = aircraft.getRegistrationNumber();
        this.modelName = aircraft.getModel().getModelName();
        this.manufacturingDate = aircraft.getManufacturingDate();
        this.activeConfigurationName = aircraft.getActiveConfigurationName();
        this.status = aircraft.getStatus().name();
        this.version = aircraft.getVersion();
    }
}
