package pt.isep.psoft.alsafe.airportmanagement.api;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.AirportViewDTO;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AirportModelAssembler extends RepresentationModelAssemblerSupport<Airport, AirportViewDTO> {

    public AirportModelAssembler() {
        super(AirportController.class, AirportViewDTO.class);
    }

    @Override
    public AirportViewDTO toModel(Airport airport) {
        AirportViewDTO dto = new AirportViewDTO(airport);
        
        // Add self link
        dto.add(linkTo(methodOn(AirportController.class).getAirportDetails(airport.getIataCode().getCode())).withSelfRel());
        
        // Add routes link
        dto.add(linkTo(methodOn(AirportController.class).getRoutesByAirport(airport.getIataCode().getCode(), 0, 10, null)).withRel("routes"));

        return dto;
    }
}
