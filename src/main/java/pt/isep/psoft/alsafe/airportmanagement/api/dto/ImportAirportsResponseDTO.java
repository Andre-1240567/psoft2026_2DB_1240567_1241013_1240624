package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import lombok.Getter;
import org.springframework.hateoas.CollectionModel;

import java.util.List;

@Getter
public class ImportAirportsResponseDTO {

    private final int totalRows;
    private final int successCount;
    private final int failureCount;
    private final CollectionModel<AirportViewDTO> createdAirports;
    private final List<AirportImportErrorDTO> errors;

    public ImportAirportsResponseDTO(int totalRows,
                                      CollectionModel<AirportViewDTO> createdAirports,
                                      List<AirportImportErrorDTO> errors) {
        this.totalRows = totalRows;
        this.createdAirports = createdAirports;
        this.errors = errors;
        this.successCount = createdAirports == null ? 0 : (int) createdAirports.getContent().size();
        this.failureCount = errors == null ? 0 : errors.size();
    }
}