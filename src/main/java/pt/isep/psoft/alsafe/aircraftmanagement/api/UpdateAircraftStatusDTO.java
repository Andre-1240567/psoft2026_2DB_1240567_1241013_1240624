package pt.isep.psoft.alsafe.aircraftmanagement.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAircraftStatusDTO {

    @NotBlank(message = "The new state is mandatory.")
    private String status;

    @NotNull(message = "The (Optimistic Locking) version is mandatory.")
    private Long version;
}
