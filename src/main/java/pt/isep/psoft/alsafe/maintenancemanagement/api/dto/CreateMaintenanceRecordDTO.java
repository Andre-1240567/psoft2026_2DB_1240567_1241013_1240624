package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateMaintenanceRecordDTO {

    @NotBlank(message = "Aircraft registration number is mandatory.")
    private String registrationNumber;

    @NotNull(message = "Maintenance template id is mandatory.")
    private Long templateId;

    @NotBlank(message = "Description is mandatory.")
    private String description;

    @NotNull(message = "Start date is mandatory.")
    private LocalDate startDate;

    @Positive(message = "Expected duration must be strictly positive.")
    private Double expectedDurationHours;

    @NotBlank(message = "Maintenance component is mandatory.")
    private String component;

    @PositiveOrZero(message = "Estimated cost cannot be negative.")
    private Double estimatedCost;
}