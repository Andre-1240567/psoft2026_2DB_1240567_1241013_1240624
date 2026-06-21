package pt.isep.psoft.alsafe.airportmanagement.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.security.access.prepost.PreAuthorize;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.*;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportImportResult;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping("/api/airports")
@Tag(name = "Airports", description = "Endpoints for managing Airports and Certifications (WP#2A)")
public class AirportController {

    private final AirportService airportService;
    private final FlightRouteService flightRouteService;
    private final AirportModelAssembler airportModelAssembler;

    public AirportController(AirportService airportService, FlightRouteService flightRouteService, AirportModelAssembler airportModelAssembler) {
        this.airportService = airportService;
        this.flightRouteService = flightRouteService;
        this.airportModelAssembler = airportModelAssembler;
    }

    @PostMapping
    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @Operation(summary = "US106 - Create a new Airport")
    public ResponseEntity<AirportViewDTO> createAirport(@Valid @RequestBody CreateAirportRequestDTO request) {
        Airport createdAirport = airportService.createAirport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(airportModelAssembler.toModel(createdAirport));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @Operation(summary = "US225 - Bulk import airports from a CSV file",
            description = "Accepts a CSV file (multipart/form-data, field name 'file'). Each row is created " +
                    "through the same path as US106 (POST /api/airports), so the same validation rules apply. " +
                    "A bad row does not block the others: the response lists which rows were created and which " +
                    "failed, with a reason for each failure.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "At least one airport was created (check 'errors' for any row that failed)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "File missing/empty, or no row could be imported")
    public ResponseEntity<ImportAirportsResponseDTO> importAirports(@RequestParam("file") MultipartFile file) {

        AirportImportResult result = airportService.importAirportsFromCsv(file);

        CollectionModel<AirportViewDTO> createdModel = airportModelAssembler.toCollectionModel(result.getCreatedAirports());

        ImportAirportsResponseDTO response = new ImportAirportsResponseDTO(
                result.getTotalRows(), createdModel, result.getErrors());

        HttpStatus status = result.getCreatedAirports().isEmpty() ? HttpStatus.BAD_REQUEST : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/{iataCode}")
    @PreAuthorize("hasAnyRole('BACKOFFICE_OPERATOR', 'ATCC')")
    @Operation(summary = "US107 - Get details of a specific Airport by IATA Code")
    public ResponseEntity<AirportViewDTO> getAirportDetails(@PathVariable("iataCode") String iataCode) {
        Airport airport = airportService.getAirportDetails(iataCode);
        return ResponseEntity.ok(airportModelAssembler.toModel(airport));
    }

    @GetMapping
    @PreAuthorize("hasRole('ATCC')")
    @Operation(summary = "US108 - Search Airports by city, country, or name")
    public ResponseEntity<CollectionModel<AirportViewDTO>> searchAirports(
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "name", required = false) String name) {
        
        if ((city == null || city.trim().isEmpty()) && 
            (country == null || country.trim().isEmpty()) && 
            (name == null || name.trim().isEmpty())) {
            throw new IllegalArgumentException("At least one search parameter (city, country, or name) is required.");
        }
        List<Airport> airports = airportService.searchAirports(city, country, name);
        return ResponseEntity.ok(airportModelAssembler.toCollectionModel(airports));
    }

    @PatchMapping("/{iataCode}/status")
    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @Operation(summary = "US109 - Change the operational status of an Airport")
    public ResponseEntity<AirportViewDTO> changeOperationalStatus(
            @PathVariable("iataCode") String iataCode,
            @Valid @RequestBody ChangeAirportStatusDTO dto) {

        Airport updatedAirport = airportService.changeOperationalStatus(iataCode, dto.getNewStatus(), dto.getVersion());
        return ResponseEntity.ok(airportModelAssembler.toModel(updatedAirport));
    }

    @PostMapping("/{iataCode}/certifications")
    @PreAuthorize("hasAnyRole('BACKOFFICE_OPERATOR', 'ATCC')")
    @Operation(summary = "US106a - Add an airplane certification to an Airport")
    public ResponseEntity<AirportViewDTO> addAirplaneCertification(
            @PathVariable("iataCode") String iataCode,
            @Valid @RequestBody pt.isep.psoft.alsafe.airportmanagement.api.dto.AddCertificationDTO dto) {

        Airport updatedAirport = airportService.addAirplaneCertification(iataCode, dto.getAircraftModelName(), dto.getVersion());
        return ResponseEntity.ok(airportModelAssembler.toModel(updatedAirport));
    }

    @PatchMapping("/{iataCode}/details")
    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @Operation(summary = "US208 - Update airport details including operational hours and contact information")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Airport details updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Airport not found")
    public ResponseEntity<AirportViewDTO> updateAirportDetails(
            @PathVariable("iataCode") String iataCode,
            @Valid @RequestBody UpdateAirportDetailsRequestDTO dto) {

        Airport updatedAirport = airportService.updateAirportDetails(iataCode, dto);
        return ResponseEntity.ok(airportModelAssembler.toModel(updatedAirport));
    }

    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/{iataCode}/routes")
    @Operation(summary = "US209 - View all routes that depart from or arrive at a specific airport")
    public ResponseEntity<PagedModel<EntityModel<FlightRouteResponseDTO>>> getRoutesByAirport(
            @PathVariable("iataCode") String iataCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            PagedResourcesAssembler<FlightRouteResponseDTO> pagedAssembler) {

        Pageable pageable = PageRequest.of(page, size);
        Page<FlightRouteResponseDTO> responsePage = flightRouteService.getRoutesByAirport(iataCode, pageable);

        return ResponseEntity.ok(pagedAssembler.toModel(responsePage));
    }

    @PreAuthorize("hasRole('BACKOFFICE_OPERATOR')")
    @GetMapping("/statistics/busiest")
    @Operation(summary = "US210 - Generate statistics on the busiest airports by number of routes")
    public ResponseEntity<CollectionModel<BusiestAirportDTO>> getBusiestAirports() {
        List<BusiestAirportDTO> stats = flightRouteService.getBusiestAirports();
        
        stats.forEach(dto -> 
            dto.add(linkTo(methodOn(AirportController.class).getAirportDetails(dto.getIataCode())).withRel("airport_details"))
        );

        CollectionModel<BusiestAirportDTO> model = CollectionModel.of(stats, 
            linkTo(methodOn(AirportController.class).getBusiestAirports()).withSelfRel());
            
        return ResponseEntity.ok(model);
    }

    @PreAuthorize("hasRole('ATCC')")
    @GetMapping("/grouped")
    @Operation(summary = "US211 - View airports grouped by region or country")
    public ResponseEntity<Map<String, CollectionModel<AirportViewDTO>>> getAirportsGroupedBy(
            @RequestParam(value = "groupBy") String groupBy) {

        Map<String, List<Airport>> groupedAirports = airportService.getAirportsGroupedBy(groupBy);
        
        Map<String, CollectionModel<AirportViewDTO>> responseMap = groupedAirports.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    List<AirportViewDTO> dtos = entry.getValue().stream()
                        .map(airportModelAssembler::toModel)
                        .toList();
                    return CollectionModel.of(dtos);
                }
            ));
            
        return ResponseEntity.ok(responseMap);
    }
}