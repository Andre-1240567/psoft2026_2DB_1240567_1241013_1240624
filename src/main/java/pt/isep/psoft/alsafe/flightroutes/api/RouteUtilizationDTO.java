package pt.isep.psoft.alsafe.flightroutes.api;

import org.springframework.hateoas.RepresentationModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RouteUtilizationDTO extends RepresentationModel<RouteUtilizationDTO> {
    private String routeId;
    private String originIata;
    private String destinationIata;
    private Long totalFlights;
}