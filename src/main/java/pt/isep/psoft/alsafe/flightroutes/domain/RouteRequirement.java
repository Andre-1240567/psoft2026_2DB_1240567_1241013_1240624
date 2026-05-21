package pt.isep.psoft.alsafe.flightroutes.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class RouteRequirement {
    
    private Double minRangeRequired;
    private Integer minCapacityRequired;

    public RouteRequirement(Double minRangeRequired, Integer minCapacityRequired) {
        if (minRangeRequired <= 0 || minCapacityRequired <= 0) {
            throw new IllegalArgumentException("The range and capacity must be greater than zero.");
        }
        this.minRangeRequired = minRangeRequired;
        this.minCapacityRequired = minCapacityRequired;
    }
}