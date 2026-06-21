package pt.isep.psoft.alsafe.flightroutes.api.dto;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

@Getter
public class NetworkDistanceResponseDTO extends RepresentationModel<NetworkDistanceResponseDTO> {

    private final Double totalDistance;

    public NetworkDistanceResponseDTO(Double totalDistance) {
        this.totalDistance = totalDistance;
    }
}