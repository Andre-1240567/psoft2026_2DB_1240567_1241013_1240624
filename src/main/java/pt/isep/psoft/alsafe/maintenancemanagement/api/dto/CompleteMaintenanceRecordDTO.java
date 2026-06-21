package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class CompleteMaintenanceRecordDTO {

    @NotBlank(message = "Completion notes are mandatory.")
    private String completionNotes;

    @NotNull(message = "Actual duration is mandatory.")
    @Positive(message = "Actual duration must be strictly positive.")
    private Double actualDurationHours;

    @PositiveOrZero(message = "Actual cost cannot be negative.")
    private Double actualCost;

    @NotNull(message = "The (Optimistic Locking) version is mandatory.")
    private Long version;
}