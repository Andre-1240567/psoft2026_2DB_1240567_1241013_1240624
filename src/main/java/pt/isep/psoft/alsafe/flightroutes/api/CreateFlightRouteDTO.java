package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Data;

// O @Data cria os Getters e Setters todos automaticamente
@Data
public class CreateFlightRouteDTO {
    private String originIata;
    private String destinationIata;
    private Double distance;
    private Integer estimatedFlightTime;
    private Double minRangeRequired;
    private Integer minCapacityRequired;
}