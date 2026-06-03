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

/**
 * REST Controller para as operações de WP#3B (Scheduled Flights).
 * @author José Alves
 */
@RestController
@RequestMapping("/api/scheduled-flights")
@RequiredArgsConstructor
@Tag(name = "Scheduled Flights", description = "Endpoints para agendamento de voos e gestão da frota nas rotas (WP#3B)")
public class ScheduledFlightController {

    private final ScheduledFlightService scheduledFlightService;
    private final ScheduledFlightModelAssembler assembler; // <-- Injeção do teu novo Assembler

    @Operation(summary = "US212: Assign an aircraft to a route for a specific date and time")
    @RolesAllowed("ATCC")
    @PostMapping
    public ResponseEntity<ScheduledFlightResponseDTO> scheduleFlight(@Valid @RequestBody CreateScheduledFlightDTO dto) {
        
        ScheduledFlight flight = scheduledFlightService.scheduleFlight(
                dto.getRouteId(),
                dto.getAircraftRegistration(),
                dto.getDepartureTime(),
                dto.getArrivalTime()
        );

        // O Assembler converte a entidade 1-para-1 e injeta o link
        return new ResponseEntity<>(assembler.toModel(flight), HttpStatus.CREATED);
    }

    @Operation(summary = "US213: View all scheduled flights for a specific aircraft")
    @RolesAllowed("ATCC")
    @GetMapping("/aircraft/{registration}")
    public ResponseEntity<CollectionModel<ScheduledFlightResponseDTO>> getFlightsByAircraft(@PathVariable String registration) {
        
        List<ScheduledFlight> flights = scheduledFlightService.getScheduledFlightsByAircraft(registration);
        
        // O método toCollectionModel() aplica o toModel() a cada item da lista automaticamente!
        return ResponseEntity.ok(assembler.toCollectionModel(flights));
    }
}