package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusiestAirportDTO extends RepresentationModel<BusiestAirportDTO> {
    private String iataCode;
    private long routeCount;
}
