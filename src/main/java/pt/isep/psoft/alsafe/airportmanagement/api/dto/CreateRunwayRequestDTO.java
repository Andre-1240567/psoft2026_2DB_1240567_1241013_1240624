package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import pt.isep.psoft.alsafe.airportmanagement.domain.Orientation;

@Getter
@Setter
public class CreateRunwayRequestDTO {
    @NotBlank(message = "Runway name is mandatory.")
    private String name;

    @NotNull(message = "Runway length is mandatory.")
    @Positive(message = "The length must be positive.")
    private Double length;

    @NotNull(message = "Runway orientation is mandatory.")
    private Orientation orientation;


}
