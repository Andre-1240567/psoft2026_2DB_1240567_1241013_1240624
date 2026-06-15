package pt.isep.psoft.alsafe.flightroutes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;
import pt.isep.psoft.alsafe.flightroutes.services.ScheduledFlightService;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/scheduled-flights")
@RequiredArgsConstructor
@Tag(name = "Scheduled Flights", description = "Endpoints para agendamento de voos e gestão da frota nas rotas (WP#3B)")
public class ScheduledFlightController {

    private final ScheduledFlightService scheduledFlightService;
    private final ScheduledFlightModelAssembler assembler;

    @Operation(summary = "US212: Assign an aircraft to a route for a specific date and time")
    @RolesAllowed("ATCC")
    @PostMapping
    public ResponseEntity<ScheduledFlightResponseDTO> scheduleFlight(
            @Valid @RequestBody CreateScheduledFlightDTO dto) {

        ScheduledFlight flight = scheduledFlightService.scheduleFlight(
                dto.getRouteId(),
                dto.getAircraftRegistration(),
                dto.getDepartureTime(),
                dto.getArrivalTime()
        );

        return new ResponseEntity<>(assembler.toModel(flight), HttpStatus.CREATED);
    }

    @Operation(summary = "US213: View all scheduled flights for a specific aircraft")
    @RolesAllowed("ATCC")
    @GetMapping("/aircraft/{registration}")
    public ResponseEntity<CollectionModel<ScheduledFlightResponseDTO>> getFlightsByAircraft(
            @PathVariable String registration) {

        List<ScheduledFlight> flights = scheduledFlightService.getScheduledFlightsByAircraft(registration);

        CollectionModel<ScheduledFlightResponseDTO> collectionModel = assembler.toCollectionModel(flights);
        collectionModel.add(linkTo(methodOn(ScheduledFlightController.class)
                .getFlightsByAircraft(registration)).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @Operation(summary = "US213: Get a scheduled flight by its flight number")
    @RolesAllowed("ATCC")
    @GetMapping("/{flightNumber}")
    public ResponseEntity<ScheduledFlightResponseDTO> getFlightById(
            @PathVariable String flightNumber) {

        ScheduledFlight flight = scheduledFlightService.getFlightById(flightNumber);
        return ResponseEntity.ok(assembler.toModel(flight));
    }
}