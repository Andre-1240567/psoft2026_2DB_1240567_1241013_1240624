package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusiestAirportDTO {
    private String iataCode;
    private long routeCount;
}
