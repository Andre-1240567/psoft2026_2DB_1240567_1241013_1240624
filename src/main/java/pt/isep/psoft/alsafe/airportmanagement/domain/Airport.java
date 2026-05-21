package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Location location;

    @ElementCollection
    @CollectionTable(
            name = "airport_runways",
            joinColumns = @JoinColumn(name = "airport_id")
    )
    private List<Runway> runways = new ArrayList<>();

    @Embedded
    @Column(unique = true, nullable = false)
    private IATACode iataCode;

    @Embedded
    private Timezone timezone;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String name;
    private String airportPhoto;

    protected Airport() {}

    public Airport(IATACode code, String name, Location location, Timezone timezone){
        if(code == null || name == null || location == null ||  timezone == null){
            throw new IllegalArgumentException("IATA Code, name and location cannot be null.");
        }
        this.iataCode = code;
        this.name = name;
        this.location = location;
        this.timezone = timezone;
    }

    public void addRunway(Runway runway){
        if(runway == null){
            throw new IllegalArgumentException("Runway cannot be null.");
        }
        this.runways.add(runway);
    }

    public void changeStatus(Status newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("O novo estado não pode ser nulo.");
        }
        if (this.status == newStatus) {
            throw new IllegalArgumentException("O aeroporto já se encontra no estado " + newStatus);
        }
        this.status = newStatus;
    }
}
