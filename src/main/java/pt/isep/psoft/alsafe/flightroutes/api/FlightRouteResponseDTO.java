package pt.isep.psoft.alsafe.flightroutes.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class FlightRouteResponseDTO {
    
    private FlightRoute data;

    @JsonProperty("_links")
    private List<LinkDTO> links = new ArrayList<>();

    public FlightRouteResponseDTO(FlightRoute route) {
        this.data = route;
        String baseUri = "/api/flight-routes/" + route.getRouteId();

        this.links.add(new LinkDTO(baseUri, "self", "GET"));

        if (route.getRouteStatus() == RouteStatus.ACTIVE) {
            this.links.add(new LinkDTO(baseUri + "/deactivate", "deactivate", "PATCH"));
            this.links.add(new LinkDTO(baseUri, "update", "PUT"));
        }
    }
}