package pt.isep.psoft.alsafe.flightroutes.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.GPSCoordinates;
import pt.isep.psoft.alsafe.airportmanagement.domain.IATACode;
import pt.isep.psoft.alsafe.airportmanagement.domain.Location;

class FlightRouteTest {

    private Airport createFakeAirport(String iata) {
        return new Airport(new IATACode(iata), "Fake Airport", 
               new Location("Reg", "Country", "City", new GPSCoordinates(0.0, 0.0)));
    }

    @Test
    void ensureOriginAndDestinationCannotBeTheSame() {
        Airport porto = createFakeAirport("OPO");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new FlightRoute("route-123", porto, porto, 0.0, 0, req, "atcc_jose");
        });

        assertEquals("A origem e o destino não podem ser o mesmo aeroporto.", exception.getMessage());
    }

    @Test
    void ensureDeactivatedRouteCannotBeUpdated() {
        Airport origin = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route = new FlightRoute("route-123", origin, destination, 300.0, 45, req, "atcc_jose");

        route.deactivate("atcc_jose"); 

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            route.updateDetails(350.0, 50, req, "atcc_jose");
        });

        assertEquals("Não podes atualizar uma rota desativada.", exception.getMessage());
    }
}