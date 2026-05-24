package pt.isep.psoft.alsafe.flightroutes.api;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateFlightRouteDTO {

    @NotBlank(message = "The IATA origin code is required.")
    @Pattern(regexp = "[A-Z]{3}", message = "The IATA origin code must contain exactly 3 uppercase letters.")
    private String originIata;

    @NotBlank(message = "The IATA destination code is required.")
    @Pattern(regexp = "[A-Z]{3}", message = "The IATA destination code must contain exactly 3 uppercase letters.")
    private String destinationIata;

    @NotNull(message = "The route's distance is mandatory.")
    @Positive(message = "The distance must be a strictly positive value.")
    private Double distance;

    @NotNull(message = "The estimated flight time is mandatory.")
    @Positive(message = "The estimated flight time should be greater than zero minutes.")
    private Integer estimatedFlightTime;

    @NotNull(message = "The minimum required range for the aircraft is mandatory.")
    @Positive(message = "The minimum required range must be a positive value.")
    private Double minRangeRequired;

    @NotNull(message = "The minimum required capacity for the aircraft is mandatory.")
    @Positive(message = "The minimum required capacity must be greater than zero seats.")
    private Integer minCapacityRequired;
}