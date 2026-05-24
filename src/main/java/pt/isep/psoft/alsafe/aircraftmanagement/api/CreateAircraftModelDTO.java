package pt.isep.psoft.alsafe.aircraftmanagement.api;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateAircraftModelDTO {

    @NotNull(message = "Manufacturer is mandatory")
    private Manufacturer manufacturer;

    @NotBlank(message = "The model name is required")
    private String modelName;

    @NotNull(message = "Seating capacity is mandatory")
    @Positive(message = "Seating capacity must be strictly positive")
    private Integer seatingCapacity;

    @Positive(message = "Fuel capacity should be positive")
    private Double fuelCapacity;

    @Positive(message = "The maximum range should be positive")
    private Double maxRange;

    @Positive(message = "The cruising speed should be positive")
    private Double cruisingSpeed;
}