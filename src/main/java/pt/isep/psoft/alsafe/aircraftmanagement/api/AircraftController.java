package pt.isep.psoft.alsafe.aircraftmanagement.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.services.AircraftService;

@RestController
@RequestMapping("/api/aircrafts")
public class AircraftController {

    private final AircraftService aircraftService;

    public AircraftController(AircraftService aircraftService) {
        this.aircraftService = aircraftService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AircraftResponseDTO> createAircraft(@Valid @RequestBody CreateAircraftDTO dto) {

        Aircraft createdAircraft = aircraftService.createAircraft(dto);
        AircraftResponseDTO responseDTO = new AircraftResponseDTO(createdAircraft);

        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }
}