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


    @Test
    void ensureConstructorRejectsMinRangeLessThanDistance() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
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


    @Test
    void ensureNullRouteIdIsRejected() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute(null, origin, destination, 280.5, 45, req, "atcc_jose"));

        assertEquals("Route ID cannot be blank.", ex.getMessage());
    }

    @Test
    void ensureBlankRouteIdIsRejected() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("   ", origin, destination, 280.5, 45, req, "atcc_jose"));

        assertEquals("Route ID cannot be blank.", ex.getMessage());
    }

    @Test
    void ensureNullOriginIsRejected() {
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-001", null, destination, 280.5, 45, req, "atcc_jose"));

        assertEquals("Origin airport cannot be null.", ex.getMessage());
    }

    @Test
    void ensureNullDestinationIsRejected() {
        Airport origin = createFakeAirport("OPO");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-001", origin, null, 280.5, 45, req, "atcc_jose"));

        assertEquals("Destination airport cannot be null.", ex.getMessage());
    }

    @Test
    void ensureNullRouteRequirementIsRejected() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-001", origin, destination, 280.5, 45, null, "atcc_jose"));

        assertEquals("Route requirement cannot be null.", ex.getMessage());
    }

    @Test
    void ensureNegativeEstimatedFlightTimeIsRejected() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-001", origin, destination, 280.5, -1, req, "atcc_jose"));

        assertEquals("Estimated flight time must be a positive value.", ex.getMessage());
    }


    @Test
    void ensureUpdateRejectsNullDistance() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route    = new FlightRoute("route-001", origin, destination, 280.5, 45, req, "atcc_jose");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                route.updateDetails(null, 45, req, "atcc_jose"));

        assertEquals("Distance must be a positive value.", ex.getMessage());
    }

    @Test
    void ensureUpdateRejectsZeroDistance() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route    = new FlightRoute("route-001", origin, destination, 280.5, 45, req, "atcc_jose");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                route.updateDetails(0.0, 45, req, "atcc_jose"));

        assertEquals("Distance must be a positive value.", ex.getMessage());
    }

    @Test
    void ensureUpdateRejectsNullEstimatedFlightTime() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route    = new FlightRoute("route-001", origin, destination, 280.5, 45, req, "atcc_jose");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                route.updateDetails(280.5, null, req, "atcc_jose"));

        assertEquals("Estimated flight time must be a positive value.", ex.getMessage());
    }

    @Test
    void ensureUpdateRejectsZeroEstimatedFlightTime() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route    = new FlightRoute("route-001", origin, destination, 280.5, 45, req, "atcc_jose");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                route.updateDetails(280.5, 0, req, "atcc_jose"));

        assertEquals("Estimated flight time must be a positive value.", ex.getMessage());
    }

    @Test
    void ensureUpdateRejectsNullRouteRequirement() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);
        FlightRoute route    = new FlightRoute("route-001", origin, destination, 280.5, 45, req, "atcc_jose");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                route.updateDetails(280.5, 45, null, "atcc_jose"));

        assertEquals("Route requirement cannot be null.", ex.getMessage());
    }

    @Test
    void ensureUpdateRejectsMinRangeLessThanDistance() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req    = new RouteRequirement(1500.0, 100);
        RouteRequirement newReq = new RouteRequirement(200.0, 100);
        FlightRoute route       = new FlightRoute("route-001", origin, destination, 280.5, 45, req, "atcc_jose");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                route.updateDetails(500.0, 45, newReq, "atcc_jose"));

        assertEquals("Minimum range required cannot be less than the route distance.", ex.getMessage());
    }
    @Test
    void ensureNullDistanceIsRejected() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-001", origin, destination, null, 45, req, "atcc_jose"));

        assertEquals("Distance must be a positive value.", ex.getMessage());
    }

    @Test
    void ensureNullEstimatedFlightTimeIsRejected() {
        Airport origin      = createFakeAirport("OPO");
        Airport destination = createFakeAirport("LIS");
        RouteRequirement req = new RouteRequirement(1500.0, 100);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new FlightRoute("route-001", origin, destination, 280.5, null, req, "atcc_jose"));

        assertEquals("Estimated flight time must be a positive value.", ex.getMessage());
    }
}