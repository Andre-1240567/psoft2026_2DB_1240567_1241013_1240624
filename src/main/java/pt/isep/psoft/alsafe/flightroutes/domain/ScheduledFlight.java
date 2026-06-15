package pt.isep.psoft.alsafe.flightroutes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "scheduled_flight")
public class ScheduledFlight {

    @Id
    private String flightNumber;

    @ManyToOne(optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private FlightRoute route;

    @ManyToOne(optional = false)
    @JoinColumn(name = "aircraft_registration", nullable = false)
    private Aircraft aircraft;

    @Column(nullable = false)
    private LocalDateTime scheduledDeparture;

    @Column(nullable = false)
    private LocalDateTime scheduledArrival;

    @Version
    private Long version;

    protected ScheduledFlight() {
    }

    public ScheduledFlight(FlightRoute route, Aircraft aircraft, LocalDateTime scheduledDeparture, LocalDateTime scheduledArrival) {
        if (route == null) {
            throw new IllegalArgumentException("Flight route cannot be null.");
        }
        if (aircraft == null) {
            throw new IllegalArgumentException("Aircraft cannot be null.");
        }
        if (scheduledDeparture == null || scheduledArrival == null) {
            throw new IllegalArgumentException("Departure and arrival times must be provided.");
        }
        if (scheduledArrival.isBefore(scheduledDeparture) || scheduledArrival.isEqual(scheduledDeparture)) {
            throw new IllegalArgumentException("Arrival time must be after departure time.");
        }

        this.flightNumber = UUID.randomUUID().toString();
        this.route = route;
        this.aircraft = aircraft;
        this.scheduledDeparture = scheduledDeparture;
        this.scheduledArrival = scheduledArrival;
    }
}