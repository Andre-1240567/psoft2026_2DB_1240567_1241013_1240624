package pt.isep.psoft.alsafe.airportmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.*;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@Tag(name = "Airports", description = "Endpoints for managing Airports and Certifications (WP#2A)")
public class AirportController {

    private final AirportService airportService;

    public AirportController(AirportService airportService) {
        this.airportService = airportService;
    }

    @PostMapping
    @Operation(summary = "US106 - Create a new Airport")
    public ResponseEntity<Airport> createAirport(@Valid @RequestBody CreateAirportRequestDTO request) {
        Airport createdAirport = airportService.createAirport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAirport);
    }

    @GetMapping("/{iataCode}")
    @Operation(summary = "US107 - Get details of a specific Airport by IATA Code")
    public ResponseEntity<Airport> getAirportDetails(@PathVariable("iataCode") String iataCode) {
        Airport airport = airportService.getAirportDetails(iataCode);
        return ResponseEntity.ok(airport);
    }

    @GetMapping
    @Operation(summary = "US108 - Search Airports by city")
    public ResponseEntity<List<Airport>> searchAirports(@RequestParam(value = "city", required = false) String city) {
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("O parâmetro de pesquisa 'city' é obrigatório.");
        }
        List<Airport> airports = airportService.searchAirportsByCity(city);
        return ResponseEntity.ok(airports);
    }

    @PatchMapping("/{iataCode}/status")
    @Operation(summary = "US109 - Change the operational status of an Airport")
    public ResponseEntity<Airport> changeOperationalStatus(
            @PathVariable("iataCode") String iataCode,
            @Valid @RequestBody ChangeAirportStatusDTO dto) {

        Airport updatedAirport = airportService.changeOperationalStatus(iataCode, dto.getNewStatus());
        return ResponseEntity.ok(updatedAirport);
    }

    @PostMapping("/{iataCode}/certifications")
    @Operation(summary = "US106a - Add an airplane certification to an Airport")
    public ResponseEntity<Airport> addAirplaneCertification(
            @PathVariable("iataCode") String iataCode,
            @Valid @RequestBody pt.isep.psoft.alsafe.airportmanagement.api.dto.AddCertificationDTO dto) {

        Airport updatedAirport = airportService.addAirplaneCertification(iataCode, dto.getAircraftModelName());
        return ResponseEntity.ok(updatedAirport);
    }

    @PatchMapping("/{iataCode}/details")
    @Operation(summary = "US208 - Update airport details including operational hours and contact information")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Airport details updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Airport not found")
    public ResponseEntity<Airport> updateAirportDetails(
            @PathVariable("iataCode") String iataCode,
            @Valid @RequestBody UpdateAirportDetailsRequestDTO dto) {

        Airport updatedAirport = airportService.updateAirportDetails(iataCode, dto);
        return ResponseEntity.ok(updatedAirport);
    }
}