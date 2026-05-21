package pt.isep.psoft.alsafe.flightroutes.api;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateFlightRouteDTO {

    @NotNull(message = "The route's distance is required for the update.")
    @Positive(message = "The distance must be a strictly positive value.")
    private Double distance;

    @NotNull(message = "The estimated flight time is required for the update.")
    @Positive(message = "The estimated flight time should be greater than zero minutes.")
    private Integer estimatedFlightTime;

    @NotNull(message = "The minimum required range is mandatory for the update.")
    @Positive(message = "The minimum required range must be a positive value.")
    private Double minRangeRequired;

    @NotNull(message = "The minimum required capacity is mandatory for the upgrade.")
    @Positive(message = "The minimum required capacity must be greater than zero seats.")
    private Integer minCapacityRequired;

    @NotNull(message = "The registry's version is mandatory to safeguard competition control.")
    private Long version;
}