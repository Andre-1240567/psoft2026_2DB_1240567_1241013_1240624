package pt.isep.psoft.alsafe.aircraftmanagement.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateAircraftDTO {

    @Pattern(regexp = "^[A-Z]{2}-[A-Z]{3,4}$", message = "Registration must follow the standard format (ex: CS-TPA).")
    @NotBlank(message = "Registration is mandatory.")
    private String registrationNumber;

    @NotBlank(message = "The model name is required.")
    private String modelName;

    @NotNull(message = "The manufacturing date is mandatory.")
    private java.time.LocalDate manufacturingDate;

    @NotBlank(message = "The active setting is required.")
    private String activeConfigurationName;
}