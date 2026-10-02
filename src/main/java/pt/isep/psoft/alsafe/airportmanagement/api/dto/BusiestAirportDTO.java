package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@EqualsAndHashCode(callSuper = false)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusiestAirportDTO extends RepresentationModel<BusiestAirportDTO> {
    private String iataCode;
    private long routeCount;
}
