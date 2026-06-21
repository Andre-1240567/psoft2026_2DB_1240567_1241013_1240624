package pt.isep.psoft.alsafe.flightroutes.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeparturesBoardResponseDTO {
    private String flightNumber;
    private LocalDateTime scheduledDeparture;
    private String destinationIata;
    private String aircraftModel;
    private String status;
}