package pt.isep.psoft.alsafe.flightroutes.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import pt.isep.psoft.alsafe.flightroutes.api.dto.AircraftUtilizationDTO;
import pt.isep.psoft.alsafe.flightroutes.services.AircraftUtilizationService;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/aircraft-utilization")
@Tag(name = "Aircraft Utilization", description = "Aircraft utilization rates over time (US223)")
public class AircraftUtilizationController {

    private final AircraftUtilizationService utilizationService;

    public AircraftUtilizationController(AircraftUtilizationService utilizationService) {
        this.utilizationService = utilizationService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ATCC')")
    @Operation(
        summary = "View utilization rates for all aircraft over time (US223)",
        description = "Returns a monthly breakdown of flight hours and number of flights " +
                      "for every aircraft in the fleet. Cancelled flights are excluded."
    )

    public ResponseEntity<CollectionModel<AircraftUtilizationDTO>> getAllAircraftUtilization() {
        List<AircraftUtilizationDTO> result = utilizationService.getUtilizationForAllAircraft();

        for (AircraftUtilizationDTO dto : result) {
            dto.add(linkTo(methodOn(AircraftUtilizationController.class)
                    .getAircraftUtilization(dto.getRegistrationNumber())).withSelfRel());
        }


        CollectionModel<AircraftUtilizationDTO> collectionModel = CollectionModel.of(result);
        

        collectionModel.add(linkTo(methodOn(AircraftUtilizationController.class)
                .getAllAircraftUtilization()).withSelfRel());

        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{registrationNumber}")
    @PreAuthorize("hasRole('ATCC')")
    @Operation(
        summary = "View utilization rate over time for a specific aircraft (US223)",
        description = "Returns a monthly breakdown of flight hours and number of flights " +
                      "for the requested aircraft. Cancelled flights are excluded."
    )
    public ResponseEntity<AircraftUtilizationDTO> getAircraftUtilization(
            @Parameter(description = "Aircraft registration number", example = "CS-TUA")
            @PathVariable String registrationNumber) {

        AircraftUtilizationDTO dto = utilizationService.getUtilizationForAircraft(registrationNumber);

        dto.add(linkTo(methodOn(AircraftUtilizationController.class)
                .getAircraftUtilization(registrationNumber)).withSelfRel());
        dto.add(linkTo(methodOn(AircraftUtilizationController.class)
                .getAllAircraftUtilization()).withRel("all-aircraft-utilization"));

        return ResponseEntity.ok(dto);
    }
}