package pt.isep.psoft.alsafe.flightroutes.api;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * Assembler responsible for converting ScheduledFlight domain entities into
 * ScheduledFlightResponseDTO representations with HATEOAS links.
 *
 * @author José Alves
 */
@Component
public class ScheduledFlightModelAssembler
        extends RepresentationModelAssemblerSupport<ScheduledFlight, ScheduledFlightResponseDTO> {

    public ScheduledFlightModelAssembler() {
        super(ScheduledFlightController.class, ScheduledFlightResponseDTO.class);
    }

    @Override
    public ScheduledFlightResponseDTO toModel(ScheduledFlight flight) {
        ScheduledFlightResponseDTO dto = new ScheduledFlightResponseDTO(
                flight.getFlightNumber(),
                flight.getRoute().getRouteIdValue(),
                flight.getAircraft().getRegistrationNumber(),
                flight.getScheduledDeparture(),
                flight.getScheduledArrival()
        );

        ScheduledFlightController ctrl = methodOn(ScheduledFlightController.class);

        dto.add(linkTo(ctrl.getFlightById(flight.getFlightNumber())).withSelfRel());

        dto.add(linkTo(ctrl.getFlightsByAircraft(
                flight.getAircraft().getRegistrationNumber()
        )).withRel("all-aircraft-flights"));

        return dto;
    }
}