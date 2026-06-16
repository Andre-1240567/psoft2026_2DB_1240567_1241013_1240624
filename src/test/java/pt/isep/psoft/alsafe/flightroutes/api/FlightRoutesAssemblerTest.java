package pt.isep.psoft.alsafe.flightroutes.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FlightRoutesAssemblerTest {

    private FlightRouteModelAssembler routeAssembler;
    private ScheduledFlightModelAssembler flightAssembler;

    private Airport origin;
    private Airport dest;

    @BeforeEach
    void setUp() {
        routeAssembler = new FlightRouteModelAssembler();
        flightAssembler = new ScheduledFlightModelAssembler();

        origin = new Airport(new IATACode("OPO"), "OPO", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        dest = new Airport(new IATACode("MAD"), "MAD", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
    }

    @Test
    void ensureRouteAssemblerAddsLinksToActiveRoute() {
        FlightRoute activeRoute = new FlightRoute("R123", origin, dest, 500.0, 60, new RouteRequirement(1000.0, 100), "admin");
        
        FlightRouteResponseDTO dto = routeAssembler.toModel(activeRoute);
        
        assertNotNull(dto);
        assertTrue(dto.getLinks().hasLink("self"));
        assertTrue(dto.getLinks().hasLink("history"));
        assertTrue(dto.getLinks().hasLink("deactivate"));
        assertTrue(dto.getLinks().hasLink("update"));
    }

    @Test
    void ensureRouteAssemblerOmitsUpdateAndDeactivateLinksForDeactivatedRoute() {
        FlightRoute deactivatedRoute = new FlightRoute("R999", origin, dest, 500.0, 60, new RouteRequirement(1000.0, 100), "admin");
        deactivatedRoute.deactivate("admin");
        
        FlightRouteResponseDTO dto = routeAssembler.toModel(deactivatedRoute);
        
        assertNotNull(dto);
        assertTrue(dto.getLinks().hasLink("self"));
        assertTrue(dto.getLinks().hasLink("history"));
        assertFalse(dto.getLinks().hasLink("deactivate"));
        assertFalse(dto.getLinks().hasLink("update"));
    }

    @Test
    void ensureScheduledFlightAssemblerAddsLinks() {
        FlightRoute route = new FlightRoute("R123", origin, dest, 500.0, 60, new RouteRequirement(1000.0, 100), "admin");
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737", 150, 10000.0, 5000.0, 800.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now().minusYears(1), "Economy");

        ScheduledFlight flight = new ScheduledFlight(route, aircraft, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

        ScheduledFlightResponseDTO dto = flightAssembler.toModel(flight);

        assertNotNull(dto);
        assertTrue(dto.getLinks().hasLink("self"));
        assertTrue(dto.getLinks().hasLink("all-aircraft-flights"));
    }
}