package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class Timezone {
    private String offsetValue;

    public Timezone() {}
    public Timezone(String offsetValue) {
        if(offsetValue == null || !offsetValue.matches("^UTC[+-](0[0-9]|1[0-4]):[0-5][0-9]$")){
            throw new IllegalArgumentException("Timezone must be in UTC format (e.g.: UTC+01:00 or UTC-05:00.");
        }
        this.offsetValue = offsetValue;
    }
}
