package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;

import java.util.List;

@Getter
public class AirportViewDTO extends RepresentationModel<AirportViewDTO> {

    private final String iataCode;
    private final String name;
    private final String region;
    private final String country;
    private final String city;
    private final String status;
    private final List<String> photos;

    public AirportViewDTO(Airport airport) {
        this.iataCode = airport.getIataCode().getCode();
        this.name = airport.getName();
        this.region = airport.getLocation().getRegion();
        this.country = airport.getLocation().getCountry();
        this.city = airport.getLocation().getCity();
        this.status = airport.getStatus().name();
        this.photos = airport.getPhotos();
    }
}
