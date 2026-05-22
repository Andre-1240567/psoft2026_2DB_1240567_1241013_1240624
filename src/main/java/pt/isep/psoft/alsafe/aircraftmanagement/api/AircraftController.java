package pt.isep.psoft.alsafe.aircraftmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.services.AircraftService;

@RestController
@RequestMapping("/api/aircrafts")
@Tag(name = "Aircrafts", description = "Endpoints for aircraft fleet management (WP#1A)")
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new aircraft instance (US102)")
    public ResponseEntity<AircraftResponseDTO> createAircraft(@Valid @RequestBody CreateAircraftDTO dto) {
        Aircraft createdAircraft = aircraftService.createAircraft(dto);
        AircraftResponseDTO responseDTO = new AircraftResponseDTO(createdAircraft);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{registrationNumber}")
    @Operation(summary = "View aircraft details by registration number (US103)")
    public ResponseEntity<AircraftResponseDTO> getAircraftDetails(@PathVariable String registrationNumber) {
        Aircraft aircraft = aircraftService.getAircraftDetails(registrationNumber);
        AircraftResponseDTO responseDTO = new AircraftResponseDTO(aircraft);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    @Operation(summary = "Search aircraft fleet by model, status, or manufacturing year (US104)")
    public ResponseEntity<java.util.List<AircraftResponseDTO>> searchAircrafts(
            @RequestParam(required = false) String modelName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year) {

        java.util.List<Aircraft> aircrafts = aircraftService.searchAircrafts(modelName, status, year);

        java.util.List<AircraftResponseDTO> responseList = new java.util.ArrayList<>();
        for (Aircraft aircraft : aircrafts) {
            responseList.add(new AircraftResponseDTO(aircraft));
        }

        return ResponseEntity.ok(responseList);
    }

    @PatchMapping("/{registrationNumber}/status")
    @Operation(summary = "Update the operational status of an aircraft (US105)")
    public ResponseEntity<AircraftResponseDTO> updateAircraftStatus(
            @PathVariable String registrationNumber,
            @Valid @RequestBody UpdateAircraftStatusDTO dto) {

        Aircraft updatedAircraft = aircraftService.updateAircraftStatus(registrationNumber, dto.getStatus(), dto.getVersion());
        return ResponseEntity.ok(new AircraftResponseDTO(updatedAircraft));
    }
}

