package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import lombok.Getter;

@Getter
public class AirportImportErrorDTO {

    private final int row;
    private final String iataCode;
    private final String message;

    public AirportImportErrorDTO(int row, String iataCode, String message) {
        this.row = row;
        this.iataCode = iataCode;
        this.message = message;
    }
}