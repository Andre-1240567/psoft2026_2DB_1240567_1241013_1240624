package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VersionedActionDTO {

    @NotNull(message = "The (Optimistic Locking) version is mandatory.")
    private Long version;
}