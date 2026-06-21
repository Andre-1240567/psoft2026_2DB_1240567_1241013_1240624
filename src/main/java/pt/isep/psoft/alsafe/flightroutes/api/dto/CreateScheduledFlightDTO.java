package pt.isep.psoft.alsafe.flightroutes.api.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class CreateScheduledFlightDTO {

    @NotBlank(message = "Route ID is mandatory")
    private String routeId;

    @NotBlank(message = "Aircraft registration is mandatory")
    private String aircraftRegistration;

    @NotNull(message = "Departure time is mandatory")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time is mandatory")
    @Future(message = "Arrival time must be in the future")
    private LocalDateTime arrivalTime;
}