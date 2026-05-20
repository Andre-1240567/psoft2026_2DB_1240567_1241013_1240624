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

    @Embedded
    private Runway runway;

    @JoinColumn(name = "airport_id")
    @ElementCollection
    @CollectionTable(name = "Runways")
    private List<Runway> runways = new ArrayList<>();

    @Embedded
    @Column(unique = true, nullable = false)
    private IATACode iataCode;

    private String name;
    private String timezone;
    private String airportPhoto;

    protected Airport() {}

    public Airport(IATACode code, String name, Location location){
        this.iataCode = code;
        this.name = name;
        this.location = location;
    }
    public void addRunway(Runway runway){
        if(runway == null){
            throw new IllegalArgumentException("Runway cannot be null");
        }
        this.runways.add(runway);
    }
}
