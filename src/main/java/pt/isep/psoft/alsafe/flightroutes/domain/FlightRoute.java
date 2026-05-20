package pt.isep.psoft.alsafe.flightroutes.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pt.isep.psoft.alsafe.airports.domain.Airport;

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

    private Double distance;
    
    private Integer estimatedFlightTime;

    @Embedded
    private RouteRequirement routeRequirement;

    @Enumerated(EnumType.STRING)
    private RouteStatus routeStatus;

    // @ElementCollection cria uma tabela extra na base de dados (flight_route_history) automaticamente
    @ElementCollection
    private List<RouteHistory> history = new ArrayList<>();

    public FlightRoute(String routeId, Airport origin, Airport destination, 
                       Double distance, Integer estimatedFlightTime, 
                       RouteRequirement routeRequirement) {
        
        if (origin.getIataCode().equals(destination.getIataCode())) {
            throw new IllegalArgumentException("A origem e o destino não podem ser o mesmo aeroporto.");
        }

        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.estimatedFlightTime = estimatedFlightTime;
        this.routeRequirement = routeRequirement;
        this.routeStatus = RouteStatus.ACTIVE;
        
        // Sempre que a rota nasce, adicionamos o primeiro registo ao histórico!
        this.addHistory("Flight Route created.");
    }

    // Método auxiliar para o futuro (US112) podermos adicionar novos registos facilmente
    public void addHistory(String description) {
        this.history.add(new RouteHistory(description));
    }

    public void deactivate() {
        if (this.routeStatus == RouteStatus.DEACTIVATED) {
            throw new IllegalStateException("The route is already deactivated");
        }
        this.routeStatus = RouteStatus.DEACTIVATED;
        this.addHistory("Flight Route deactivated.");
    }

    public void updateDetails(Double distance, Integer estimatedFlightTime, RouteRequirement routeRequirement) {
        if (this.routeStatus == RouteStatus.DEACTIVATED) {
            throw new IllegalStateException("You can't update a deactivated Route");
        }
        
        this.distance = distance;
        this.estimatedFlightTime = estimatedFlightTime;
        this.routeRequirement = routeRequirement;
        
        this.addHistory("Flight Route details updated."); // Histórico a trabalhar pra nós!
    }
}