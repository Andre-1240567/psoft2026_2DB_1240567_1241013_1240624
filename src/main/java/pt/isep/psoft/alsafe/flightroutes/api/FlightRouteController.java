package pt.isep.psoft.alsafe.flightroutes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;

import java.util.List;

@Tag(name = "Flight Routes", description = "Endpoints for managing flight routes (WP#3A)")
@RestController
@RequestMapping("/api/flight-routes")
public class FlightRouteController {

    private final FlightRouteService flightRouteService;
    private final FlightRouteModelAssembler assembler;

    public FlightRouteController(FlightRouteService flightRouteService,
                                 FlightRouteModelAssembler assembler) {
        this.flightRouteService = flightRouteService;
        this.assembler = assembler;
    }

    @Operation(summary = "Create a flight route",
               description = "Creates a new flight route between two OPERATIONAL airports. Requires ATCC role.")
    @PreAuthorize("hasRole('ATCC')")
    @PostMapping
    public ResponseEntity<FlightRouteResponseDTO> createRoute(@Valid @RequestBody CreateFlightRouteDTO dto) {
        return new ResponseEntity<>(flightRouteService.createFlightRoute(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get history of a flight route",
               description = "Returns the change history of a flight route. Requires ATCC role.")
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<FlightRouteResponseDTO.RouteHistoryDTO>> getRouteHistory(
            @PathVariable("id") String routeId) {
        return ResponseEntity.ok(flightRouteService.getRouteHistory(routeId));
    }

    @Operation(summary = "Deactivate a flight route",
               description = "Deactivates an active flight route. Requires ATCC or BACKOFFICE_OPERATOR role.")
    @PreAuthorize("hasRole('ATCC') or hasRole('BACKOFFICE_OPERATOR')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<FlightRouteResponseDTO> deactivateRoute(@PathVariable("id") String routeId) {
        return ResponseEntity.ok(flightRouteService.deactivateRoute(routeId));
    }

    @Operation(summary = "Update a flight route",
               description = "Updates the details of an active flight route. Requires ATCC or BACKOFFICE_OPERATOR role.")
    @PreAuthorize("hasRole('ATCC') or hasRole('BACKOFFICE_OPERATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<FlightRouteResponseDTO> updateRoute(
            @PathVariable("id") String routeId,
            @Valid @RequestBody UpdateFlightRouteDTO dto) {
        return ResponseEntity.ok(flightRouteService.updateRoute(routeId, dto));
    }

    @Operation(summary = "Get a flight route by ID",
               description = "Returns the details of a specific flight route. Requires ATCC role.")
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/{id}")
    public ResponseEntity<FlightRouteResponseDTO> getRouteById(@PathVariable("id") String routeId) {
        return ResponseEntity.ok(flightRouteService.getRouteById(routeId));
    }

    @Operation(summary = "Search flight routes",
               description = "Returns a paginated list of flight routes with HATEOAS links. Requires ATCC role.")
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<FlightRouteResponseDTO>>> searchRoutes(
            @RequestParam(required = false) String originIata,
            @RequestParam(required = false) String destinationIata,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<FlightRouteResponseDTO> pagedAssembler) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FlightRouteResponseDTO> responsePage =
                flightRouteService.searchRoutes(originIata, destinationIata, pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(responsePage));
    }
}