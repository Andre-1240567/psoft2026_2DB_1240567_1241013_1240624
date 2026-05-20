package pt.isep.psoft.alsafe.flightroutes.api;

import com.fasterxml.jackson.annotation.JsonProperty; // Import importante!
import lombok.Data;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;

import java.util.ArrayList;
import java.util.List;

@Data
public class FlightRouteResponseDTO {
    
    private FlightRoute data;

    // O @JsonProperty força o Jackson a escrever "_links" no JSON,
    // mas a variável em Java fica com um nome normal que o Lombok entende!
    @JsonProperty("_links")
    private List<LinkDTO> links = new ArrayList<>();

    public FlightRouteResponseDTO(FlightRoute route) {
        this.data = route;
        String baseUri = "/api/flight-routes/" + route.getRouteId();

        // Adicionamos os links à lista normal
        this.links.add(new LinkDTO(baseUri, "self", "GET"));

        // Os links mudam dinamicamente dependendo do ESTADO da rota!
        if (route.getRouteStatus() == RouteStatus.ACTIVE) {
            this.links.add(new LinkDTO(baseUri + "/deactivate", "deactivate", "PATCH"));
            this.links.add(new LinkDTO(baseUri, "update", "PUT"));
        }
    }
}