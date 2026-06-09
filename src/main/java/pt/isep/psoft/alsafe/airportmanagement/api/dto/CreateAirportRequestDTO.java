package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import jakarta.validation.Valid;
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

    @NotBlank(message = "A timezone é obrigatória.")
    @Pattern(regexp = "^UTC[+-](0[0-9]|1[0-4]):[0-5][0-9]$", message = "Invalid format. Use e.g.: UTC+01:00")
    private String timezone;

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
    
    private List<String> photos;
    
    @Valid
    private List<CreateTerminalRequestDTO> terminals;
}
