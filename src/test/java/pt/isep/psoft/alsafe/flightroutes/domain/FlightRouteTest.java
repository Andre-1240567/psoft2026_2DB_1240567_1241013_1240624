package pt.isep.psoft.alsafe.flightroutes.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import pt.isep.psoft.alsafe.airportmanagement.domain.*;

class FlightRouteTest {

    private Airport createFakeAirport(String iata) {
        return new Airport(
                new IATACode(iata),
                "Fake Airport",
                new Location("Reg", "Country", "City", new GPSCoordinates(0.0, 0.0)),
                new Timezone("UTC+00:00"));
    }

    @Test
    void ensureValidFlightRouteIsCreatedSuccessfully() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        // minRangeRequired (1500) >= distance (280.5) — valid
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        FlightRoute route = assertDoesNotThrow(() ->
                new FlightRoute("route-001", origin, destination, 280.5, 45, req, "atcc_jose"));

        assertEquals("route-001",        route.getRouteIdValue());
        assertEquals(280.5,              route.getDistance());
        assertEquals(45,                 route.getEstimatedFlightTime());
        assertEquals(RouteStatus.ACTIVE, route.getRouteStatus());
        assertNotNull(route.getHistory());
        assertEquals(1, route.getHistory().size());
    }

    @Test
    void ensureOriginAndDestinationCannotBeTheSame() {
        Airport porto        = createFakeAirport("OPO");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-123", porto, porto, 280.5, 45, req, "atcc_jose"));

        assertEquals("The origin and destination cannot be the same airport.", ex.getMessage());
    }

    @Test
    void ensureZeroDistanceIsRejected() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-002", origin, destination, 0.0, 45, req, "atcc_jose"));
    }

    @Test
    void ensureNegativeDistanceIsRejected() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-003", origin, destination, -1.0, 45, req, "atcc_jose"));
    }

    @Test
    void ensureZeroEstimatedFlightTimeIsRejected() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-004", origin, destination, 280.5, 0, req, "atcc_jose"));
    }

    /**
     * NEW — was missing before. Guards that a route cannot be born with an impossible
     * requirement: minRangeRequired < distance means no aircraft could ever fly it.
     */
    @Test
    void ensureConstructorRejectsMinRangeLessThanDistance() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        // distance = 1000, minRange = 500 → impossible requirement
        RouteRequirement req = new RouteRequirement(500.0, 100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-005", origin, destination, 1000.0, 90, req, "atcc_jose"));

        assertEquals("Minimum range required cannot be less than the route distance.", ex.getMessage());
    }

    @Test
    void ensureDeactivatedRouteCannotBeUpdated() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route    = new FlightRoute("route-123", origin, destination, 300.0, 45, req, "atcc_jose");

        route.deactivate("atcc_jose");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                route.updateDetails(350.0, 50, req, "atcc_jose"));

        assertEquals("Cannot update a deactivated route.", ex.getMessage());
    }

    @Test
    void ensureActiveRouteCanBeUpdated() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req    = new RouteRequirement(1500.0, 100);
        RouteRequirement newReq = new RouteRequirement(2000.0, 200);
        FlightRoute route       = new FlightRoute("route-123", origin, destination, 300.0, 45, req, "atcc_jose");

        assertDoesNotThrow(() -> route.updateDetails(350.0, 50, newReq, "atcc_jose"));

        assertEquals(350.0, route.getDistance());
        assertEquals(50,    route.getEstimatedFlightTime());
        assertEquals(2, route.getHistory().size());
    }

    @Test
    void ensureActiveRouteCanBeDeactivated() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route    = new FlightRoute("route-123", origin, destination, 300.0, 45, req, "atcc_jose");

        route.deactivate("atcc_jose");

        assertEquals(RouteStatus.DEACTIVATED, route.getRouteStatus());
    }

    @Test
    void ensureAlreadyDeactivatedRouteCannotBeDeactivatedAgain() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route    = new FlightRoute("route-123", origin, destination, 300.0, 45, req, "atcc_jose");

        route.deactivate("atcc_jose");

        assertThrows(IllegalStateException.class, () -> route.deactivate("atcc_jose"));
    }
}