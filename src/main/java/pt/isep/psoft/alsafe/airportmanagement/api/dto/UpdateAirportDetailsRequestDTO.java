package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateAirportDetailsRequestDTO {
    @Valid
    private OperationalHoursDTO operationalHours;

    @Valid
    private List<ContactDTO> contacts;
}
