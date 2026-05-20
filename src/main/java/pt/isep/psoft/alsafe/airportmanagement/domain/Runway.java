package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Embeddable
public class Runway {

    @Enumerated(EnumType.STRING)
    private Orientation orientation;

    protected Runway() {}

    private String name;
    private Double length;

    public Runway(String name, Double length, Orientation orientation) {
        if(orientation == null){
            throw new IllegalArgumentException("The orientation is mandatory.");
        }
        this.name = name;
        this.length = length;
        this.orientation = orientation;
    }

}
