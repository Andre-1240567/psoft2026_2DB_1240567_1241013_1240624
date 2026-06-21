package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeAirportStatusDTO {
    @NotBlank(message = "The new status is mandatory.")
    private String newStatus;

    @NotNull(message = "The version is mandatory for optimistic locking.")
    private Long version;
}
