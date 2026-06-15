package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String designation;

    @ElementCollection
    @CollectionTable(
            name = "terminal_gates",
            joinColumns = @JoinColumn(name = "terminal_id")
    )
    private List<Gate> gates = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "terminal_services",
            joinColumns = @JoinColumn(name = "terminal_id")
    )
    private List<FacilityService> services = new ArrayList<>();

    protected Terminal() {}

    public Terminal(String designation) {
        if (designation == null || designation.trim().isEmpty()) {
            throw new IllegalArgumentException("Terminal designation cannot be empty.");
        }
        this.designation = designation;
    }

    public void addGate(Gate gate) {
        if (gate == null) {
            throw new IllegalArgumentException("Gate cannot be null.");
        }
        this.gates.add(gate);
    }

    public void addService(FacilityService service) {
        if (service == null) {
            throw new IllegalArgumentException("Service cannot be null.");
        }
        this.services.add(service);
    }
}
