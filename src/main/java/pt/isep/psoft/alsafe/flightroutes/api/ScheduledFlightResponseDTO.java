package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
public class ScheduledFlightResponseDTO extends RepresentationModel<ScheduledFlightResponseDTO> {

    private String flightNumber;
    private String routeId;
    private String aircraftRegistration;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;
    private String status;

    public ScheduledFlightResponseDTO(String flightNumber, String routeId, String aircraftRegistration,
                                      LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival, String status) {
        this.flightNumber = flightNumber;
        this.routeId = routeId;
        this.aircraftRegistration = aircraftRegistration;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
        this.status = status;
    }
}