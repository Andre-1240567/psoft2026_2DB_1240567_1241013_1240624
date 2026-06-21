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

import pt.isep.psoft.alsafe.flightroutes.api.dto.AlternativeRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.CreateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.NetworkDistanceResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.RouteUtilizationDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.UpdateFlightRouteDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Flight Routes", description = "Endpoints for managing flight routes (WP#3A & WP#3B)")
@RestController
@RequestMapping("/api/flight-routes")
public class FlightRouteController {

    private final FlightRouteService flightRouteService;

    public FlightRouteController(FlightRouteService flightRouteService) {
        this.flightRouteService = flightRouteService;
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

    @Operation(summary = "Search and list flight routes (US114 & US214)",
               description = "Returns a paginated, HATEOAS-enriched list of routes. " +
                             "Can filter by originIata, destinationIata, status (e.g. ACTIVE), " +
                             "and sort by 'popularity' or 'distance'. Requires ATCC role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Paginated list of routes returned"),
        @ApiResponse(responseCode = "400", description = "Invalid sort parameter provided"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<FlightRouteResponseDTO>>> searchRoutes(
            @RequestParam(required = false) String originIata,
            @RequestParam(required = false) String destinationIata,
            @RequestParam(required = false) RouteStatus status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<FlightRouteResponseDTO> pagedAssembler) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FlightRouteResponseDTO> responsePage;

        if (sortBy != null) {
            RouteStatus filterStatus = status != null ? status : RouteStatus.ACTIVE;
            responsePage = flightRouteService.getActiveRoutesSorted(filterStatus, sortBy, pageable);
        } else {
            responsePage = flightRouteService.searchRoutes(originIata, destinationIata, pageable);
        }

        return ResponseEntity.ok(pagedAssembler.toModel(responsePage));
    }

    @Operation(summary = "US215: Calculate total network distance",
               description = "Calculates the total distance covered by all ACTIVE flight routes in the network. Requires ATCC role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total distance calculated successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/network/total-distance")
    public ResponseEntity<NetworkDistanceResponseDTO> getTotalNetworkDistance() {
        
        Double totalDistance = flightRouteService.getTotalNetworkDistance();
        NetworkDistanceResponseDTO response = new NetworkDistanceResponseDTO(totalDistance);

        response.add(linkTo(methodOn(FlightRouteController.class).getTotalNetworkDistance()).withSelfRel());
        
        response.add(linkTo(FlightRouteController.class).withRel("all_routes"));

        return ResponseEntity.ok(response);
    }


    @Operation(summary = "US216: Search for alternative routes",
               description = "Finds alternative combinations of active flight routes connecting two airports via layovers. Defaults to fewest-stops algorithm. Requires ATCC role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of alternative routes returned successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid algorithm or same origin and destination"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required"),
        @ApiResponse(responseCode = "404", description = "Origin or destination airport not found")
    })
    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/alternatives")
    public ResponseEntity<List<AlternativeRouteResponseDTO>> getAlternativeRoutes(
            @RequestParam String originIata,
            @RequestParam String destinationIata,
            @RequestParam(defaultValue = "fewest-stops") String algorithm) {

        if (originIata.equalsIgnoreCase(destinationIata)) {
            throw new IllegalArgumentException("Origin and destination must be different.");
        }

        List<AlternativeRouteResponseDTO> alternatives = flightRouteService.findAlternativeRoutes(originIata, destinationIata, algorithm);
        
        alternatives.forEach(alt -> alt.add(
                linkTo(methodOn(FlightRouteController.class).getAlternativeRoutes(originIata, destinationIata, algorithm)).withSelfRel()
        ));

        return ResponseEntity.ok(alternatives);
    }

    @Operation(summary = "Bonus US229: Flight utilization report",
            description = "Returns all routes ranked by number of scheduled (non-cancelled) flights. Requires BACKOFFICE_OPERATOR role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Utilization report returned successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — BACKOFFICE_OPERATOR required")
    })
    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @GetMapping("/reports/utilization")
    public ResponseEntity<List<RouteUtilizationDTO>> getRouteUtilizationReport() {
        List<RouteUtilizationDTO> report = flightRouteService.getRouteUtilizationReport();
        report.forEach(dto -> dto.add(
            linkTo(methodOn(FlightRouteController.class).getRouteById(dto.getRouteId())).withRel("route")
        ));
        return ResponseEntity.ok(report);
    }


    @Operation(summary = "Bonus US228: Export route network data",
            description = "Exports all active routes in GeoJSON or KML format. Requires BACKOFFICE_OPERATOR role.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Export returned successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid format — use 'geojson' or 'kml'"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — BACKOFFICE_OPERATOR required")
    })
    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @GetMapping("/export")
    public ResponseEntity<String> exportRouteNetwork(@RequestParam String format) {
        return switch (format.toLowerCase()) {
            case "geojson" -> ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/geo+json"))
                    .body(flightRouteService.exportGeoJson());
            case "kml" -> ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.google-earth.kml+xml"))
                    .body(flightRouteService.exportKml());
            default -> ResponseEntity.badRequest().body("Invalid format. Use 'geojson' or 'kml'.");
        };
    }

    
}