package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OperationalHoursDTO {
    @NotNull
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Format must be HH:mm")
    @Schema(example = "08:00")
    private String openingTime;

    @NotNull
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Format must be HH:mm")
    @Schema(example = "22:00")
    private String closingTime;
}
