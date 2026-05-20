package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Data;

@Data
public class UpdateFlightRouteDTO {
    private Double distance;
    private Integer estimatedFlightTime;
    private Double minRangeRequired;
    private Integer minCapacityRequired;
}