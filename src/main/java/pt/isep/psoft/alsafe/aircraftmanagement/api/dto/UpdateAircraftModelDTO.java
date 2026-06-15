package pt.isep.psoft.alsafe.aircraftmanagement.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAircraftModelDTO {

    @NotNull
    @Positive
    private Integer seatingCapacity;

    @NotNull
    @Positive
    private Double fuelCapacity;

    @NotNull
    @Positive
    private Double maxRange;

    @NotNull
    @Positive
    private Double cruisingSpeed;
    
    @NotNull
    private Long version;
}
