package pt.isep.psoft.alsafe.aircraftmanagement.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAircraftDTO {

    @NotBlank(message = "Registration number is mandatory (e.g., CS-TPA).")
    private String registrationNumber;

    @NotBlank(message = "Model name is mandatory to link the aircraft.")
    private String modelName;

    @NotNull(message = "Manufacturing date is mandatory.")
    @PastOrPresent(message = "Manufacturing date cannot be in the future.")
    private LocalDate manufacturingDate;

    @NotBlank(message = "Active configuration name is mandatory.")
    private String activeConfigurationName;
}