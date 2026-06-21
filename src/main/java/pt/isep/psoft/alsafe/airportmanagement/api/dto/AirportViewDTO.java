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
    
    private final List<TerminalViewDTO> terminals;

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
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                setClosingTime(airport.getOperationalHours().getClosingTime()
                    .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
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

        this.terminals = airport.getTerminals().stream()
            .map(TerminalViewDTO::new)
            .collect(Collectors.toList());
    }


    @Getter
    public static class TerminalViewDTO {
        private final String designation;
        private final List<String> gates;
        private final List<ServiceViewDTO> services;

        public TerminalViewDTO(pt.isep.psoft.alsafe.airportmanagement.domain.Terminal terminal) {
            this.designation = terminal.getDesignation();
            
            this.gates = terminal.getGates().stream()
                    .map(pt.isep.psoft.alsafe.airportmanagement.domain.Gate::getDesignation)
                    .collect(Collectors.toList());
                    
            this.services = terminal.getServices().stream()
                    .map(ServiceViewDTO::new)
                    .collect(Collectors.toList());
        }
    }

    @Getter
    public static class ServiceViewDTO {
        private final String serviceType;
        private final String description;

        public ServiceViewDTO(pt.isep.psoft.alsafe.airportmanagement.domain.FacilityService service) {
            this.serviceType = service.getServiceType();
            this.description = service.getDescription();
        }
    }
}