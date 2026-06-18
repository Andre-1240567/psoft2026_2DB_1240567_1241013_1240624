package pt.isep.psoft.alsafe.flightroutes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.flightroutes.services.FuelEfficiencyService;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/fuel-efficiency")
@RequiredArgsConstructor
@Tag(name = "Fuel Efficiency", description = "Fuel efficiency metrics per aircraft and per route (US227)")
public class FuelEfficiencyController {

    private final FuelEfficiencyService fuelEfficiencyService;

    @Operation(
        summary = "US227: Fuel efficiency metrics for all aircraft",
        description = "Returns estimated fuel consumption and efficiency (km/L) for every aircraft " +
                      "that has at least one non-cancelled scheduled flight. " +
                      "Fuel burn rate is derived from AircraftModel.fuelCapacity / AircraftModel.maxRange. " +
                      "Requires ATCC role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fuel efficiency data returned successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required")
    })
    @GetMapping("/aircraft")
    @PreAuthorize("hasRole('ATCC')")
    public ResponseEntity<CollectionModel<AircraftFuelEfficiencyDTO>> getAllAircraftEfficiency() {

        List<AircraftFuelEfficiencyDTO> dtos = fuelEfficiencyService.getEfficiencyForAllAircraft();

        for (AircraftFuelEfficiencyDTO dto : dtos) {
            dto.add(linkTo(methodOn(FuelEfficiencyController.class)
                    .getAircraftEfficiency(dto.getRegistrationNumber())).withSelfRel());
        }

        CollectionModel<AircraftFuelEfficiencyDTO> collection = CollectionModel.of(dtos);
        collection.add(linkTo(methodOn(FuelEfficiencyController.class)
                .getAllAircraftEfficiency()).withSelfRel());
        collection.add(linkTo(methodOn(FuelEfficiencyController.class)
                .getAllRouteEfficiency()).withRel("route-efficiency"));

        return ResponseEntity.ok(collection);
    }

    @Operation(
        summary = "US227: Fuel efficiency metrics for a specific aircraft",
        description = "Returns estimated fuel consumption and efficiency (km/L) for a given aircraft " +
                      "across all its non-cancelled scheduled flights. Requires ATCC role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fuel efficiency data returned successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required"),
        @ApiResponse(responseCode = "404", description = "Aircraft not found")
    })
    @GetMapping("/aircraft/{registration}")
    @PreAuthorize("hasRole('ATCC')")
    public ResponseEntity<AircraftFuelEfficiencyDTO> getAircraftEfficiency(
            @Parameter(description = "Aircraft registration number", example = "CS-TUA")
            @PathVariable String registration) {

        AircraftFuelEfficiencyDTO dto =
                fuelEfficiencyService.getEfficiencyForAircraft(registration);

        dto.add(linkTo(methodOn(FuelEfficiencyController.class)
                .getAircraftEfficiency(registration)).withSelfRel());
        dto.add(linkTo(methodOn(FuelEfficiencyController.class)
                .getAllAircraftEfficiency()).withRel("all-aircraft-efficiency"));

        return ResponseEntity.ok(dto);
    }

    @Operation(
        summary = "US227: Fuel efficiency metrics for all routes",
        description = "Returns estimated fuel consumption per flight and efficiency (km/L) for every " +
                      "route that has at least one non-cancelled scheduled flight. " +
                      "When multiple aircraft models operated a route, the burn rate is averaged. " +
                      "Requires ATCC role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fuel efficiency data returned successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required")
    })
    @GetMapping("/routes")
    @PreAuthorize("hasRole('ATCC')")
    public ResponseEntity<CollectionModel<RouteFuelEfficiencyDTO>> getAllRouteEfficiency() {

        List<RouteFuelEfficiencyDTO> dtos = fuelEfficiencyService.getEfficiencyForAllRoutes();

        for (RouteFuelEfficiencyDTO dto : dtos) {
            dto.add(linkTo(methodOn(FuelEfficiencyController.class)
                    .getRouteEfficiency(dto.getRouteId())).withSelfRel());
        }

        CollectionModel<RouteFuelEfficiencyDTO> collection = CollectionModel.of(dtos);
        collection.add(linkTo(methodOn(FuelEfficiencyController.class)
                .getAllRouteEfficiency()).withSelfRel());
        collection.add(linkTo(methodOn(FuelEfficiencyController.class)
                .getAllAircraftEfficiency()).withRel("aircraft-efficiency"));

        return ResponseEntity.ok(collection);
    }

    @Operation(
        summary = "US227: Fuel efficiency metrics for a specific route",
        description = "Returns estimated fuel consumption per flight and efficiency (km/L) for a given " +
                      "route across all its non-cancelled scheduled flights. Requires ATCC role."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fuel efficiency data returned successfully"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
        @ApiResponse(responseCode = "403", description = "Insufficient role — ATCC required"),
        @ApiResponse(responseCode = "404", description = "Route not found")
    })
    @GetMapping("/routes/{routeId}")
    @PreAuthorize("hasRole('ATCC')")
    public ResponseEntity<RouteFuelEfficiencyDTO> getRouteEfficiency(
            @Parameter(description = "Flight route ID", example = "LIS-OPO-001")
            @PathVariable String routeId) {

        RouteFuelEfficiencyDTO dto = fuelEfficiencyService.getEfficiencyForRoute(routeId);

        dto.add(linkTo(methodOn(FuelEfficiencyController.class)
                .getRouteEfficiency(routeId)).withSelfRel());
        dto.add(linkTo(methodOn(FuelEfficiencyController.class)
                .getAllRouteEfficiency()).withRel("all-route-efficiency"));

        return ResponseEntity.ok(dto);
    }
}