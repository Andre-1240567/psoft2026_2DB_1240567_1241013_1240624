package pt.isep.psoft.alsafe.airportmanagement.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.CreateAirportRequestDTO;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.ChangeAirportStatusDTO; // DTO novo para a US109
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
public class AirportController {

    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @PostMapping
    public ResponseEntity<Airport> createAirport(@Valid @RequestBody CreateAirportRequestDTO request) {
        Airport createdAirport = airportService.createAirport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAirport);
    }

    @GetMapping("/{iataCode}")
    public ResponseEntity<Airport> getAirportDetails(@PathVariable("iataCode") String iataCode) {
        Airport airport = airportService.getAirportDetails(iataCode);
        return ResponseEntity.ok(airport);
    }

    @GetMapping
    public ResponseEntity<List<Airport>> searchAirports(@RequestParam(value = "city", required = false) String city) {
        // Se enviarem a cidade, pesquisa. Se não enviarem nada, devolve erro (ou podia devolver todos)
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("O parâmetro de pesquisa 'city' é obrigatório.");
        }

        List<Airport> airports = airportService.searchAirportsByCity(city);
        return ResponseEntity.ok(airports);
    }

    // --- US109: MUDAR ESTADO OPERACIONAL ---
    @PatchMapping("/{iataCode}/status")
    public ResponseEntity<Airport> changeOperationalStatus(
            @PathVariable("iataCode") String iataCode,
            @Valid @RequestBody ChangeAirportStatusDTO dto) {

        Airport updatedAirport = airportService.changeOperationalStatus(iataCode, dto.getNewStatus());
        return ResponseEntity.ok(updatedAirport);
    }
}