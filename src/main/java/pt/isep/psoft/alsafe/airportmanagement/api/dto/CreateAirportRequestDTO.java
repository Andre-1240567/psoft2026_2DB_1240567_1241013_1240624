package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateAirportRequestDTO {

    @NotBlank(message = "The IATA code is mandatory.")
    @Pattern(regexp = "^[A-Z]{3}$", message = "The IATA code must be formed from 3 uppercase letters.")
    private String iataCode;

    @NotBlank(message ="The airport name is mandatory.")
    private String name;

    @NotBlank
    private String region;

    @NotBlank
    private String city;

    @NotBlank
    private String country;

    private Double latitude;
    private Double longitude;

    @NotNull(message = "The airport must have at least one runway.")
    private List<CreateRunwayRequestDTO> runways;
}
