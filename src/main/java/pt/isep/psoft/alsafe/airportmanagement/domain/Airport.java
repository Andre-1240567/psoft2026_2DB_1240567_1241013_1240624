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

    @ElementCollection
    private List<AirplaneCertification> certifications = new ArrayList<>();

    private String name;
    
    @ElementCollection
    @CollectionTable(
            name = "airport_photos",
            joinColumns = @JoinColumn(name = "airport_id")
    )
    private List<String> photos = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "airport_id")
    private List<Terminal> terminals = new ArrayList<>();

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

    public void addTerminal(Terminal terminal){
        if(terminal == null){
            throw new IllegalArgumentException("Terminal cannot be null.");
        }
        this.terminals.add(terminal);
    }
    
    public void addPhoto(String photoUrl){
        if(photoUrl == null || photoUrl.trim().isEmpty()){
             throw new IllegalArgumentException("Photo URL cannot be empty.");
        }
        this.photos.add(photoUrl);
    }

    public void changeStatus(Status newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("The new state cannot be null.");
        }
        if (this.status == newStatus) {
            throw new IllegalArgumentException("The airport is already in that state " + newStatus);
        }
        this.status = newStatus;
    }

    public void addCertification(String modelName) {
        boolean alreadyCertified = certifications.stream()
                .anyMatch(cert -> cert.getModelName().equals(modelName));

        if (alreadyCertified) {
            throw new IllegalArgumentException("The airport already is certified for this model: " + modelName);
        }

        this.certifications.add(new AirplaneCertification(modelName, java.time.LocalDate.now()));
    }
}
