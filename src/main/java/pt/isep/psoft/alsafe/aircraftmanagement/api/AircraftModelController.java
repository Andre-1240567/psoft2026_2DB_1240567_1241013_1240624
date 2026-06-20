package pt.isep.psoft.alsafe.aircraftmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import pt.isep.psoft.alsafe.aircraftmanagement.services.AircraftModelService;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import org.springframework.security.access.prepost.PreAuthorize;
import pt.isep.psoft.alsafe.aircraftmanagement.api.dto.UpdateAircraftModelDTO;

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
    public java.util.List<AircraftModel> getAllAircraftModels() {
        return service.getAllAircraftModels();
    }

    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new aircraft model (US101)")
    public AircraftModel create(@Valid @RequestBody CreateAircraftModelDTO dto) {
        return service.createAircraftModel(dto);
    }

    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @PutMapping("/{id}")
    @Operation(summary = "Update an aircraft model's specifications (US201)")
    public AircraftModel update(@PathVariable Long id, @Valid @RequestBody UpdateAircraftModelDTO dto) {
        return service.updateAircraftModel(id, dto);
    }

    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @GetMapping("/top-utilized")
    @Operation(summary = "Get top 5 most utilized aircraft models (US204)")
    public java.util.List<pt.isep.psoft.alsafe.aircraftmanagement.api.dto.TopAircraftModelDTO> getTopUtilizedModels(@RequestParam(defaultValue = "hours") String criteria) {
        return service.getTop5MostUtilizedModels(criteria);
    }
}