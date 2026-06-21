package pt.isep.psoft.alsafe.aircraftmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import pt.isep.psoft.alsafe.aircraftmanagement.services.AircraftModelService;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.api.dto.AircraftModelResponseDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.api.dto.UpdateAircraftModelDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.api.dto.TopAircraftModelDTO;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/aircraft-models")
@Tag(name = "Aircraft Models", description = "Endpoints for aircraft models management (WP#1A)")
public class AircraftModelController {

    private final AircraftModelService service;

    public AircraftModelController(AircraftModelService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @GetMapping
    @Operation(summary = "Get all aircraft models")
    public ResponseEntity<List<AircraftModelResponseDTO>> getAllAircraftModels() {
        List<AircraftModelResponseDTO> result = service.getAllAircraftModels().stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new aircraft model (US101/US202)")
    public ResponseEntity<AircraftModelResponseDTO> create(@Valid @RequestBody CreateAircraftModelDTO dto) {
        AircraftModel created = service.createAircraftModel(dto);
        AircraftModelResponseDTO responseDTO = toResponseDTO(created);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @PutMapping("/{id}")
    @Operation(summary = "Update an aircraft model's specifications (US201)")
    public ResponseEntity<AircraftModelResponseDTO> update(@PathVariable Long id, @Valid @RequestBody UpdateAircraftModelDTO dto) {
        AircraftModel updated = service.updateAircraftModel(id, dto);
        AircraftModelResponseDTO responseDTO = toResponseDTO(updated);
        return ResponseEntity.ok(responseDTO);
    }

    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @GetMapping("/top-utilized")
    @Operation(summary = "Get top 5 most utilized aircraft models (US204)")
    public ResponseEntity<List<TopAircraftModelDTO>> getTopUtilizedModels(@RequestParam(defaultValue = "hours") String criteria) {
        List<TopAircraftModelDTO> result = service.getTop5MostUtilizedModels(criteria);
        for (TopAircraftModelDTO dto : result) {
            dto.add(linkTo(methodOn(AircraftModelController.class).getAllAircraftModels()).withRel("all-models"));
        }
        return ResponseEntity.ok(result);
    }

    private AircraftModelResponseDTO toResponseDTO(AircraftModel model) {
        AircraftModelResponseDTO dto = new AircraftModelResponseDTO(model);
        dto.add(linkTo(methodOn(AircraftModelController.class).getAllAircraftModels()).withRel("all-models"));
        return dto;
    }
}