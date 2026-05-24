package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCertificationDTO {
    @NotBlank(message = "Model name is mandatory.")
    private String aircraftModelName;
}