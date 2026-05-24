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
        if (minRangeRequired == null || minRangeRequired <= 0) {
            throw new IllegalArgumentException("Minimum range required must be a positive value.");
        }
        if (minCapacityRequired == null || minCapacityRequired <= 0) {
            throw new IllegalArgumentException("Minimum capacity required must be a positive value.");
        }
        this.minRangeRequired = minRangeRequired;
        this.minCapacityRequired = minCapacityRequired;
    }
}