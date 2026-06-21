package pt.isep.psoft.alsafe.airportmanagement.services;

import pt.isep.psoft.alsafe.airportmanagement.api.dto.AirportImportErrorDTO;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;

import java.util.List;

/**
 * Internal result of AirportService#importAirportsFromCsv.
 * Kept as plain domain/data objects (no HATEOAS) so the service layer stays
 * independent of the web layer; the controller is responsible for assembling
 * the final ImportAirportsResponseDTO with links.
 */
public class AirportImportResult {

    private final int totalRows;
    private final List<Airport> createdAirports;
    private final List<AirportImportErrorDTO> errors;

    public AirportImportResult(int totalRows, List<Airport> createdAirports, List<AirportImportErrorDTO> errors) {
        this.totalRows = totalRows;
        this.createdAirports = createdAirports;
        this.errors = errors;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public List<Airport> getCreatedAirports() {
        return createdAirports;
    }

    public List<AirportImportErrorDTO> getErrors() {
        return errors;
    }
}