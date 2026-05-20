package pt.isep.psoft.alsafe.aircraftmanagement.api;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import pt.isep.psoft.alsafe.aircraftmanagement.services.AircraftModelService;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;

@RestController
@RequestMapping("/api/aircraft-models")
public class AircraftModelController {

    private final AircraftModelService service;

    public AircraftModelController(AircraftModelService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AircraftModel create(@Valid @RequestBody CreateAircraftModelDTO dto) {
        return service.createAircraftModel(dto);
    }
}