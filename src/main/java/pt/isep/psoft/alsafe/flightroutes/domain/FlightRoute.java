package pt.isep.psoft.alsafe.flightroutes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;

import java.util.ArrayList;
import java.util.List;

@Entity
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
    private List<RouteHistory> history = new ArrayList<>();

    public FlightRoute(String routeId, Airport origin, Airport destination, 
                       Double distance, Integer estimatedFlightTime, 
                       RouteRequirement routeRequirement, String author) {
        
        if (origin.getIataCode().getCode().equals(destination.getIataCode().getCode())) {
            throw new IllegalArgumentException("The origin and destination cannot be from the same airport.");
        }

        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.estimatedFlightTime = estimatedFlightTime;
        this.routeRequirement = routeRequirement;
        this.routeStatus = RouteStatus.ACTIVE;
        
        this.addHistory("Flight Route created.", author);
    }

    public void addHistory(String description, String author) {
        this.history.add(new RouteHistory(description, author));
    }

    public void deactivate(String author) {
        if (this.routeStatus == RouteStatus.DEACTIVATED) {
            throw new IllegalStateException("The route is already deactivated.");
        }
        this.routeStatus = RouteStatus.DEACTIVATED;
        this.addHistory("Flight Route deactivated.", author);
    }

    public void updateDetails(Double distance, Integer estimatedFlightTime, RouteRequirement routeRequirement, String author) {
        if (this.routeStatus == RouteStatus.DEACTIVATED) {
            throw new IllegalStateException("You cannot update a disabled route.");
        }
        
        this.distance = distance;
        this.estimatedFlightTime = estimatedFlightTime;
        this.routeRequirement = routeRequirement;
        
        this.addHistory("Flight Route details updated.", author);
    }
}