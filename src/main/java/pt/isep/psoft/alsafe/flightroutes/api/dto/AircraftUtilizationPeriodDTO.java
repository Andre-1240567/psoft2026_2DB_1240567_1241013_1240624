package pt.isep.psoft.alsafe.flightroutes.api.dto;

import lombok.Getter;

@Getter
public class AircraftUtilizationPeriodDTO {

    private final int year;
    private final int month;
    private final long totalFlights;
    private final double totalFlightHours;

    public AircraftUtilizationPeriodDTO(int year, int month, long totalFlights, double totalFlightHours) {
        this.year = year;
        this.month = month;
        this.totalFlights = totalFlights;
        this.totalFlightHours = Math.round(totalFlightHours * 100.0) / 100.0;
    }
}