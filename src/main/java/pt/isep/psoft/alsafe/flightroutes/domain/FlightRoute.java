package pt.isep.psoft.alsafe.flightroutes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "flight_route")
@Getter
@NoArgsConstructor
public class FlightRoute {

    @Id
    private String routeId;

    @ManyToOne(optional = false)
    private Airport origin;

    @ManyToOne(optional = false)
    private Airport destination;

    @Version
    private Long version;

    private Double distance;

    private Integer estimatedFlightTime;

    @Embedded
    private RouteRequirement routeRequirement;

    @Enumerated(EnumType.STRING)
    private RouteStatus routeStatus;

    @ElementCollection
    @CollectionTable(name = "flight_route_history", joinColumns = @JoinColumn(name = "route_id"))
    private List<RouteHistory> history = new ArrayList<>();

    public FlightRoute(String routeId, Airport origin, Airport destination,
                       Double distance, Integer estimatedFlightTime,
                       RouteRequirement routeRequirement, String author) {

        // Separate null-checks so the error message identifies exactly which argument is null
        if (origin == null) {
            throw new IllegalArgumentException("Origin airport cannot be null.");
        }
        if (destination == null) {
            throw new IllegalArgumentException("Destination airport cannot be null.");
        }
        if (origin.getIataCode().getCode().equals(destination.getIataCode().getCode())) {
            throw new IllegalArgumentException("The origin and destination cannot be the same airport.");
        }
        if (distance == null || distance <= 0) {
            throw new IllegalArgumentException("Distance must be a positive value.");
        }
        if (estimatedFlightTime == null || estimatedFlightTime <= 0) {
            throw new IllegalArgumentException("Estimated flight time must be a positive value.");
        }
        if (routeRequirement == null) {
            throw new IllegalArgumentException("Route requirement cannot be null.");
        }

        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.estimatedFlightTime = estimatedFlightTime;
        this.routeRequirement = routeRequirement;
        this.routeStatus = RouteStatus.ACTIVE;

        this.addHistory("Flight route created.", author);
    }

    public void addHistory(String description, String author) {
        this.history.add(new RouteHistory(description, author));
    }

    public void deactivate(String author) {
        if (this.routeStatus == RouteStatus.DEACTIVATED) {
            throw new IllegalStateException("The route is already deactivated.");
        }
        this.routeStatus = RouteStatus.DEACTIVATED;
        this.addHistory("Flight route deactivated.", author);
    }

    public void updateDetails(Double distance, Integer estimatedFlightTime,
                              RouteRequirement routeRequirement, String author) {
        if (this.routeStatus == RouteStatus.DEACTIVATED) {
            throw new IllegalStateException("Cannot update a deactivated route.");
        }
        if (distance == null || distance <= 0) {
            throw new IllegalArgumentException("Distance must be a positive value.");
        }
        if (estimatedFlightTime == null || estimatedFlightTime <= 0) {
            throw new IllegalArgumentException("Estimated flight time must be a positive value.");
        }
        if (routeRequirement == null) {
            throw new IllegalArgumentException("Route requirement cannot be null.");
        }

        this.distance = distance;
        this.estimatedFlightTime = estimatedFlightTime;
        this.routeRequirement = routeRequirement;

        this.addHistory("Flight route details updated.", author);
    }
}