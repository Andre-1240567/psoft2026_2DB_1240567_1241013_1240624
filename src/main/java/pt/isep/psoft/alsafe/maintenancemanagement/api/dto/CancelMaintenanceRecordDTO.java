package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CancelMaintenanceRecordDTO {

    @NotBlank(message = "A cancellation reason is mandatory.")
    private String reason;

    @NotNull(message = "The (Optimistic Locking) version is mandatory.")
    private Long version;
}