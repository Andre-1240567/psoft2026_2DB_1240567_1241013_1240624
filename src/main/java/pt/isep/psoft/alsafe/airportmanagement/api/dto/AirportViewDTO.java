package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class AirportViewDTO extends RepresentationModel<AirportViewDTO> {

    private final String iataCode;
    private final String name;
    private final String region;
    private final String country;
    private final String city;
    private final String status;
    private final List<String> photos;
    private final List<String> certifications;
    private final OperationalHoursDTO operationalHours;
    private final List<ContactDTO> contacts;

    public AirportViewDTO(Airport airport) {
        this.iataCode = airport.getIataCode().getCode();
        this.name = airport.getName();
        this.region = airport.getLocation().getRegion();
        this.country = airport.getLocation().getCountry();
        this.city = airport.getLocation().getCity();
        this.status = airport.getStatus().name();
        this.photos = airport.getPhotos();
        this.certifications = airport.getCertifications().stream()
                .map(c -> c.getModelName())
                .collect(Collectors.toList());
        this.operationalHours = airport.getOperationalHours() != null
            ? new OperationalHoursDTO() {{
                setOpeningTime(airport.getOperationalHours().getOpeningTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                setClosingTime(airport.getOperationalHours().getClosingTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
            }}
            : null;
        this.contacts = airport.getContacts().stream()
            .map(c -> {
                ContactDTO dto = new ContactDTO();
                dto.setValue(c.getValue());
                dto.setDepartment(c.getDepartment());
                dto.setType(c.getType());
            return dto;
            })
            .collect(Collectors.toList());
    }
}
