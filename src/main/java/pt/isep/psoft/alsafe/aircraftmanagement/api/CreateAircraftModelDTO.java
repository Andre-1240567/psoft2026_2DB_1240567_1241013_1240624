package pt.isep.psoft.alsafe.aircraftmanagement.api;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAircraftModelDTO {
    @NotBlank(message = "Manufacturer is required")
    private String manufacturer;

    @NotBlank(message = "The model name is required")
    private String modelName;

    @Positive(message = "Fuel capacity should be positive")
    private Double fuelCapacity;

    @Positive(message = "The maximum range should be positive")
    private Double maxRange;

    @Positive(message = "The cruising speed should be positive")
    private Double cruisingSpeed;
}