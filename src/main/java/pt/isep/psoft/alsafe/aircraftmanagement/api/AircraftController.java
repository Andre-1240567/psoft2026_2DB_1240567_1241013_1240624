package pt.isep.psoft.alsafe.aircraftmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.services.AircraftService;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/aircrafts")
@Tag(name = "Aircrafts", description = "Endpoints for aircraft fleet management (WP#1A)")
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @PreAuthorize("hasRole('ATCC')") //US102
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new aircraft instance (US102)")
    public ResponseEntity<AircraftResponseDTO> createAircraft(@Valid @RequestBody CreateAircraftDTO dto) {
        Aircraft createdAircraft = aircraftService.createAircraft(dto);
        AircraftResponseDTO responseDTO = new AircraftResponseDTO(createdAircraft);

        responseDTO.add(linkTo(methodOn(AircraftController.class).getAircraftDetails(responseDTO.getRegistrationNumber())).withSelfRel());

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ATCC', 'BACKOFFICE_OPERATOR')") //US103
    @GetMapping("/{registrationNumber}")
    @Operation(summary = "View aircraft details by registration number (US103)")
    public ResponseEntity<AircraftResponseDTO> getAircraftDetails(@PathVariable String registrationNumber) {
        Aircraft aircraft = aircraftService.getAircraftDetails(registrationNumber);
        AircraftResponseDTO responseDTO = new AircraftResponseDTO(aircraft);

        responseDTO.add(linkTo(methodOn(AircraftController.class).getAircraftDetails(registrationNumber)).withSelfRel());

        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('ATCC')") //US104
    @GetMapping
    @Operation(summary = "Search aircraft fleet by model, status, or manufacturing year (US104)")
    public ResponseEntity<java.util.List<AircraftResponseDTO>> searchAircrafts(
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year) {

        java.util.List<Aircraft> aircrafts = aircraftService.searchAircrafts(modelName, status, year);

        java.util.List<AircraftResponseDTO> responseList = new java.util.ArrayList<>();
        for (Aircraft aircraft : aircrafts) {
            AircraftResponseDTO dto = new AircraftResponseDTO(aircraft);

            dto.add(linkTo(methodOn(AircraftController.class).getAircraftDetails(dto.getRegistrationNumber())).withSelfRel());
            responseList.add(dto);
        }

        return ResponseEntity.ok(responseList);
    }

    @PreAuthorize("hasRole('ATCC')") //S105
    @PatchMapping("/{registrationNumber}/status")
    @Operation(summary = "Update the operational status of an aircraft (US105)")
    public ResponseEntity<AircraftResponseDTO> updateAircraftStatus(
            @PathVariable String registrationNumber,
            @Valid @RequestBody UpdateAircraftStatusDTO dto) {

        Aircraft updatedAircraft = aircraftService.updateAircraftStatus(registrationNumber, dto.getStatus(), dto.getVersion());
        AircraftResponseDTO responseDTO = new AircraftResponseDTO(updatedAircraft);

        responseDTO.add(linkTo(methodOn(AircraftController.class).getAircraftDetails(responseDTO.getRegistrationNumber())).withSelfRel());

        return ResponseEntity.ok(responseDTO);
    }
}