package pt.isep.psoft.alsafe.flightroutes.services.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.IATACode;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EcoFriendlyRoutingStrategyTest {

    @Mock
    private FlightRouteRepository routeRepository;

    private EcoFriendlyRoutingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new EcoFriendlyRoutingStrategy(routeRepository);
    }

    @Test
    void ensureCorrectAlgorithmNameIsReturned() {
        assertEquals("eco-friendly", strategy.getAlgorithmName());
    }

    private Airport mockAirport(String iataCode) {
        Airport a = mock(Airport.class);
        IATACode code = mock(IATACode.class);
        lenient().when(code.getCode()).thenReturn(iataCode);
        lenient().when(a.getIataCode()).thenReturn(code);
        return a;
    }

    private FlightRoute mockRoute(Airport origin, Airport destination, double distance) {
        FlightRoute r = mock(FlightRoute.class);
        lenient().when(r.getOrigin()).thenReturn(origin);
        lenient().when(r.getDestination()).thenReturn(destination);
        lenient().when(r.getDistance()).thenReturn(distance);
        lenient().when(r.getRouteStatus()).thenReturn(RouteStatus.ACTIVE);
        return r;
    }

    @Test
    void ensureEcoFriendlyAlgorithmFindsShortestPathByDistance() {
        Airport opo = mockAirport("OPO");
        Airport lis = mockAirport("LIS");
        Airport mad = mockAirport("MAD");

        FlightRoute r1 = mockRoute(opo, mad, 1000.0);
        FlightRoute r2 = mockRoute(mad, lis, 500.0);
        FlightRoute r3 = mockRoute(opo, lis, 300.0);

        when(routeRepository.findAll()).thenReturn(List.of(r1, r2, r3));

        List<List<FlightRoute>> alternatives = strategy.findAlternatives("OPO", "LIS");

        assertEquals(2, alternatives.size());
        
        List<FlightRoute> bestPath = alternatives.get(0);
        assertEquals(1, bestPath.size());
        assertEquals(r3, bestPath.get(0));

        List<FlightRoute> worstPath = alternatives.get(1);
        assertEquals(2, worstPath.size());
        assertEquals(r1, worstPath.get(0));
        assertEquals(r2, worstPath.get(1));
    }

    @Test
    void ensureDepthLimitPreventsInfinitePaths() {
        Airport opo = mockAirport("OPO");
        Airport a   = mockAirport("AAA");
        Airport b   = mockAirport("BBB");
        Airport c   = mockAirport("CCC");
        Airport d   = mockAirport("DDD");
        Airport lis = mockAirport("LIS");

        FlightRoute r1 = mockRoute(opo, a,   100.0);
        FlightRoute r2 = mockRoute(a,   b,   100.0);
        FlightRoute r3 = mockRoute(b,   c,   100.0);
        FlightRoute r4 = mockRoute(c,   d,   100.0);
        FlightRoute r5 = mockRoute(d,   lis, 100.0);

        when(routeRepository.findAll()).thenReturn(List.of(r1, r2, r3, r4, r5));

        List<List<FlightRoute>> alternatives = strategy.findAlternatives("OPO", "LIS");

        assertTrue(alternatives.isEmpty());
    }

    @Test
    void ensureInactiveRoutesAreIgnored() {
        Airport opo = mockAirport("OPO");
        Airport lis = mockAirport("LIS");

        FlightRoute inactiveRoute = mock(FlightRoute.class);
        lenient().when(inactiveRoute.getOrigin()).thenReturn(opo);
        lenient().when(inactiveRoute.getDestination()).thenReturn(lis);
        lenient().when(inactiveRoute.getDistance()).thenReturn(300.0);
        when(inactiveRoute.getRouteStatus()).thenReturn(RouteStatus.DEACTIVATED);

        when(routeRepository.findAll()).thenReturn(List.of(inactiveRoute));

        List<List<FlightRoute>> alternatives = strategy.findAlternatives("OPO", "LIS");

        assertTrue(alternatives.isEmpty());
    }

    @Test
    void ensureCyclesAreNotFollowed() {
        Airport opo = mockAirport("OPO");
        Airport mad = mockAirport("MAD");
        Airport lis = mockAirport("LIS");

        FlightRoute opoToMad = mockRoute(opo, mad, 500.0);
        FlightRoute madToOpo = mockRoute(mad, opo, 500.0);
        FlightRoute madToLis = mockRoute(mad, lis, 200.0);

        when(routeRepository.findAll()).thenReturn(List.of(opoToMad, madToOpo, madToLis));

        List<List<FlightRoute>> alternatives = strategy.findAlternatives("OPO", "LIS");

        assertEquals(1, alternatives.size());
        assertEquals(2, alternatives.get(0).size());
        assertEquals(opoToMad, alternatives.get(0).get(0));
        assertEquals(madToLis, alternatives.get(0).get(1));
    }
}