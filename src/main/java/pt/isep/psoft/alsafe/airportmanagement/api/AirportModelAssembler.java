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

        String iata = airport.getIataCode().getCode();

        // Add self link
        dto.add(linkTo(methodOn(AirportController.class).getAirportDetails(iata)).withSelfRel());

        // Add routes link (CORRIGIDO: Construção de URL segura para não colapsar com nulos)
        dto.add(linkTo(AirportController.class).slash(iata).slash("routes").withRel("routes"));

        return dto;
    }
}