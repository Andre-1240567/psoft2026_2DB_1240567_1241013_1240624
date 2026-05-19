package pt.isep.psoft.alsafe.airportmanagement.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Getter;

@Getter
@Embeddable
public class Location {
    private String region;
    private String city;
    private String country;

    @Embedded
    private GPSCoordinates coordinates;

    protected Location(){}
    public Location(String region, String country, String city, GPSCoordinates coordinates) {
        if(region == null || region.isEmpty() || city == null || city.isEmpty() || country == null || country.isEmpty()){
            throw new IllegalArgumentException("The region, city and country are mandatory.");
        }
        this.region = region;
        this.city = city;
        this.country = country;
        this.coordinates = coordinates;
    }
}
