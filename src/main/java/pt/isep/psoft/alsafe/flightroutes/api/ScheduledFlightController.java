package pt.isep.psoft.alsafe.flightroutes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "US212: Assign an aircraft to a route for a specific date and time",
               description = "Creates a new scheduled flight ensuring no time overlap for the aircraft. Requires ATCC role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Scheduled flight created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or arrival time before departure"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required"),
        @ApiResponse(responseCode = "404", description = "Flight route or aircraft not found"),
        @ApiResponse(responseCode = "409", description = "Concurrency conflict: Aircraft already scheduled for another flight during this timeframe")
    })
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

    @Operation(summary = "US213: View all scheduled flights for a specific aircraft",
               description = "Returns a HATEOAS collection of all scheduled flights associated with a given aircraft registration. Requires ATCC role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of scheduled flights returned successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required"),
        @ApiResponse(responseCode = "404", description = "Aircraft not found")
    })
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

    @Operation(summary = "US213: Get a scheduled flight by its flight number",
               description = "Returns a specific scheduled flight using its generated flight number. Requires ATCC role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Scheduled flight returned successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required"),
        @ApiResponse(responseCode = "404", description = "Scheduled flight not found")
    })
    @RolesAllowed("ATCC")
    @GetMapping("/{flightNumber}")
    public ResponseEntity<ScheduledFlightResponseDTO> getFlightById(
            @PathVariable String flightNumber) {

        ScheduledFlight flight = scheduledFlightService.getFlightById(flightNumber);
        return ResponseEntity.ok(assembler.toModel(flight));
    }
}