package pt.isep.psoft.alsafe.aircraftmanagement.api;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/aircraft-models")
public class AircraftModelController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public String create(@RequestBody CreateAircraftModelDTO dto) {
        return "Model " + dto.getModelName() + "successfully received!";
    }
}