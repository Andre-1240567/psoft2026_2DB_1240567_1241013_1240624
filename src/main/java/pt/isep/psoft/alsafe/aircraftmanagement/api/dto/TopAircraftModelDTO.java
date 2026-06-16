package pt.isep.psoft.alsafe.aircraftmanagement.api.dto;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopAircraftModelDTO {
    private AircraftModel aircraftModel;
    private Double utilizationValue;
}
