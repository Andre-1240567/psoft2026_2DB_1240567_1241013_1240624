package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCertificationDTO {
    @NotBlank(message = "Model name is mandatory.")
    private String aircraftModelName;

    @NotNull(message = "The version is mandatory for optimistic locking.")
    private Long version;
}