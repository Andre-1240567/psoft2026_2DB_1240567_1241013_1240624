package pt.isep.psoft.alsafe.flightroutes.api;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FlightRouteModelAssembler
        extends RepresentationModelAssemblerSupport<FlightRoute, FlightRouteResponseDTO> {

    public FlightRouteModelAssembler() {
        super(FlightRouteController.class, FlightRouteResponseDTO.class);
    }

    @Override
    public FlightRouteResponseDTO toModel(FlightRoute route) {
        FlightRouteResponseDTO dto = new FlightRouteResponseDTO(route);

        FlightRouteController ctrl = methodOn(FlightRouteController.class);

        dto.add(linkTo(ctrl.getRouteById(route.getRouteIdValue())).withSelfRel());
        dto.add(linkTo(ctrl.getRouteHistory(route.getRouteIdValue())).withRel("history"));

        if (route.getRouteStatus() == RouteStatus.ACTIVE) {
            dto.add(linkTo(ctrl.deactivateRoute(route.getRouteIdValue())).withRel("deactivate"));
            dto.add(linkTo(ctrl.updateRoute(route.getRouteIdValue(), new UpdateFlightRouteDTO())).withRel("update"));
        }

        return dto;
    }
}