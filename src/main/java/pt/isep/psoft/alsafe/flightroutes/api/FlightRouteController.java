package pt.isep.psoft.alsafe.flightroutes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Flight route created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data, non-operational airport, or same origin and destination"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required"),
        @ApiResponse(responseCode = "404", description = "Origin or destination airport not found")
    })
    @PreAuthorize("hasRole('ATCC')")
    @PostMapping
    public ResponseEntity<FlightRouteResponseDTO> createRoute(@Valid @RequestBody CreateFlightRouteDTO dto) {
        return new ResponseEntity<>(flightRouteService.createFlightRoute(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get history of a flight route",
               description = "Returns the full change history of a flight route. Requires ATCC role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "History returned successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required"),
        @ApiResponse(responseCode = "404", description = "Flight route not found")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/{id}/history")
    public ResponseEntity<List<FlightRouteResponseDTO.RouteHistoryDTO>> getRouteHistory(
            @PathVariable("id") String routeId) {
        return ResponseEntity.ok(flightRouteService.getRouteHistory(routeId));
    }

    @Operation(summary = "Deactivate a flight route",
               description = "Permanently deactivates an active flight route. Requires ATCC or BACKOFFICE_OPERATOR role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Route deactivated successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC or BACKOFFICE_OPERATOR required"),
        @ApiResponse(responseCode = "404", description = "Flight route not found"),
        @ApiResponse(responseCode = "409", description = "Route is already deactivated")
    })
    @PreAuthorize("hasRole('ATCC') or hasRole('BACKOFFICE_OPERATOR')")
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<FlightRouteResponseDTO> deactivateRoute(@PathVariable("id") String routeId) {
        return ResponseEntity.ok(flightRouteService.deactivateRoute(routeId));
    }

    @Operation(summary = "Update a flight route",
               description = "Updates the details of an active flight route. Requires ATCC or BACKOFFICE_OPERATOR role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Route updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC or BACKOFFICE_OPERATOR required"),
        @ApiResponse(responseCode = "404", description = "Flight route not found"),
        @ApiResponse(responseCode = "409", description = "Concurrency conflict (stale version) or route is deactivated")
    })
    @PreAuthorize("hasRole('ATCC') or hasRole('BACKOFFICE_OPERATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<FlightRouteResponseDTO> updateRoute(
            @PathVariable("id") String routeId,
            @Valid @RequestBody UpdateFlightRouteDTO dto) {
        return ResponseEntity.ok(flightRouteService.updateRoute(routeId, dto));
    }

    @Operation(summary = "Get a flight route by ID",
               description = "Returns the full details (including history) of a specific flight route. Requires ATCC role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Route found and returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required"),
        @ApiResponse(responseCode = "404", description = "Flight route not found")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/{id}")
    public ResponseEntity<FlightRouteResponseDTO> getRouteById(@PathVariable("id") String routeId) {
        return ResponseEntity.ok(flightRouteService.getRouteById(routeId));
    }

    @Operation(summary = "Search flight routes",
               description = "Returns a paginated, HATEOAS-enriched list of routes. " +
                             "Filter by originIata, destinationIata, both, or neither. Requires ATCC role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paginated list of routes returned"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required")
    })
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