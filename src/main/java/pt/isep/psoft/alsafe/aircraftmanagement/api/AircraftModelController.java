package pt.isep.psoft.alsafe.aircraftmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import pt.isep.psoft.alsafe.aircraftmanagement.services.AircraftModelService;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;

@RestController
@RequestMapping("/api/aircraft-models")
@Tag(name = "Aircraft Models", description = "Endpoints for aircraft models management (WP#1A)")
public class AircraftModelController {

    private final AircraftModelService service;

    public AircraftModelController(AircraftModelService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new aircraft model (US101)")
    public AircraftModel create(@Valid @RequestBody CreateAircraftModelDTO dto) {
        return service.createAircraftModel(dto);
    }
}