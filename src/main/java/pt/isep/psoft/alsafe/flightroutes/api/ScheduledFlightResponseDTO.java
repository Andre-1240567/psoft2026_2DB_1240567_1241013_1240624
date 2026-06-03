package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.time.LocalDateTime;

/**
 * DTO de resposta que representa um voo agendado com links HATEOAS.
 * @author José Alves
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ScheduledFlightResponseDTO extends RepresentationModel<ScheduledFlightResponseDTO> {
    
    private String flightNumber;
    private String routeId;
    private String aircraftRegistration;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime scheduledArrival;

    // Construtor explícito (resolve o erro "The constructor ... is undefined")
    public ScheduledFlightResponseDTO(String flightNumber, String routeId, String aircraftRegistration, LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        this.flightNumber = flightNumber;
        this.routeId = routeId;
        this.aircraftRegistration = aircraftRegistration;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
    }
}