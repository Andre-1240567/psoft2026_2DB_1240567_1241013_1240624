package pt.isep.psoft.alsafe.flightroutes.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Value Object representing the unique identifier of a FlightRoute.
 * Mirrors the IATACode pattern used in the airport aggregate.
 */
@Embeddable
@Getter
@NoArgsConstructor
public class RouteId {

    private String id;

    public RouteId(final String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Route ID cannot be blank.");
        }
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RouteId other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}