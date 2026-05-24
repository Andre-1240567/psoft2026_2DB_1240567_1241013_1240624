package pt.isep.psoft.alsafe.flightroutes.api;

import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteHistory;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
public class FlightRouteResponseDTO extends RepresentationModel<FlightRouteResponseDTO> {

    private final String routeId;
    private final String originIataCode;
    private final String destinationIataCode;
    private final Double distance;
    private final Integer estimatedFlightTime;
    private final Double minRangeRequired;
    private final Integer minCapacityRequired;
    private final RouteStatus routeStatus;
    private final Long version;
    private final List<RouteHistoryDTO> history;

    public FlightRouteResponseDTO(FlightRoute route) {
        this.routeId             = route.getRouteId();
        this.originIataCode      = route.getOrigin().getIataCode().getCode();
        this.destinationIataCode = route.getDestination().getIataCode().getCode();
        this.distance            = route.getDistance();
        this.estimatedFlightTime = route.getEstimatedFlightTime();
        this.minRangeRequired    = route.getRouteRequirement().getMinRangeRequired();
        this.minCapacityRequired = route.getRouteRequirement().getMinCapacityRequired();
        this.routeStatus         = route.getRouteStatus();
        this.version             = route.getVersion();
        
        // This safely triggers lazy loading ONLY if executed inside the Service's @Transactional boundary
        this.history             = route.getHistory().stream()
                                        .map(RouteHistoryDTO::new)
                                        .toList();

        FlightRouteController ctrl = methodOn(FlightRouteController.class);

        this.add(linkTo(ctrl.getRouteById(route.getRouteId())).withSelfRel());
        this.add(linkTo(ctrl.getRouteHistory(route.getRouteId())).withRel("history"));

        if (route.getRouteStatus() == RouteStatus.ACTIVE) {
            this.add(linkTo(ctrl.deactivateRoute(route.getRouteId())).withRel("deactivate"));
            this.add(linkTo(ctrl.updateRoute(route.getRouteId(), new UpdateFlightRouteDTO())).withRel("update"));
        }
    }

    @Getter
    public static class RouteHistoryDTO {
        private final LocalDateTime changeDate; // Changed to proper native date type
        private final String description;
        private final String author;

        public RouteHistoryDTO(RouteHistory history) {
            this.changeDate  = history.getChangeDate();
            this.description = history.getDescription();
            this.author      = history.getAuthor();
        }
    }
}