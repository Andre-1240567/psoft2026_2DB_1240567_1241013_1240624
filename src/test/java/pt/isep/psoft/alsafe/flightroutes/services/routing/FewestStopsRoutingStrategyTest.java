package pt.isep.psoft.alsafe.flightroutes.services.routing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.IATACode;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FewestStopsRoutingStrategyTest {

    @Mock
    private FlightRouteRepository routeRepository;

    private FewestStopsRoutingStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new FewestStopsRoutingStrategy(routeRepository);
    }

    @Test
    void ensureCorrectAlgorithmNameIsReturned() {
        assertEquals("fewest-stops", strategy.getAlgorithmName());
    }

    private Airport mockAirport(String iataCode) {
        Airport a = mock(Airport.class);
        IATACode code = mock(IATACode.class);
        lenient().when(code.getCode()).thenReturn(iataCode);
        lenient().when(a.getIataCode()).thenReturn(code);
        return a;
    }

    private FlightRoute mockRoute(Airport origin, Airport destination) {
        FlightRoute r = mock(FlightRoute.class);
        lenient().when(r.getOrigin()).thenReturn(origin);
        lenient().when(r.getDestination()).thenReturn(destination);
        lenient().when(r.getRouteStatus()).thenReturn(RouteStatus.ACTIVE);
        return r;
    }

    @Test
    void ensureFewestStopsAlgorithmFindsShortestPathByStops() {
        Airport opo = mockAirport("OPO");
        Airport lis = mockAirport("LIS");
        Airport mad = mockAirport("MAD");
        Airport cdg = mockAirport("CDG");

        FlightRoute r1 = mockRoute(opo, mad);
        FlightRoute r2 = mockRoute(mad, lis);

        FlightRoute r3 = mockRoute(opo, lis);

        FlightRoute r4 = mockRoute(opo, cdg);
        FlightRoute r5 = mockRoute(cdg, mad);

        when(routeRepository.findAll()).thenReturn(List.of(r1, r2, r3, r4, r5));

        List<List<FlightRoute>> alternatives = strategy.findAlternatives("OPO", "LIS");

        assertEquals(3, alternatives.size()); 

        assertEquals(1, alternatives.get(0).size());
        assertEquals(r3, alternatives.get(0).get(0));

        assertEquals(2, alternatives.get(1).size());
        assertEquals(r1, alternatives.get(1).get(0));
        assertEquals(r2, alternatives.get(1).get(1));

        assertEquals(3, alternatives.get(2).size());
        assertEquals(r4, alternatives.get(2).get(0));
    }

    @Test
    void ensureInactiveRoutesAreIgnored() {
        Airport opo = mockAirport("OPO");
        Airport lis = mockAirport("LIS");

        FlightRoute inactiveRoute = mock(FlightRoute.class);
        lenient().when(inactiveRoute.getOrigin()).thenReturn(opo);
        lenient().when(inactiveRoute.getDestination()).thenReturn(lis);
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

        FlightRoute opoToMad = mockRoute(opo, mad);
        FlightRoute madToOpo = mockRoute(mad, opo);
        FlightRoute madToLis = mockRoute(mad, lis);

        when(routeRepository.findAll()).thenReturn(List.of(opoToMad, madToOpo, madToLis));

        List<List<FlightRoute>> alternatives = strategy.findAlternatives("OPO", "LIS");

        assertEquals(1, alternatives.size());
        assertEquals(2, alternatives.get(0).size());
        assertEquals(opoToMad, alternatives.get(0).get(0));
        assertEquals(madToLis, alternatives.get(0).get(1));
    }

    @Test
    void ensureMaxDepthPreventsPathsWithMoreThanThreeRoutes() {
        Airport opo = mockAirport("OPO");
        Airport a   = mockAirport("AAA");
        Airport b   = mockAirport("BBB");
        Airport c   = mockAirport("CCC");
        Airport lis = mockAirport("LIS");

        FlightRoute r1 = mockRoute(opo, a);
        FlightRoute r2 = mockRoute(a,   b);
        FlightRoute r3 = mockRoute(b,   c);
        FlightRoute r4 = mockRoute(c,   lis);
        when(routeRepository.findAll()).thenReturn(List.of(r1, r2, r3, r4));

        List<List<FlightRoute>> alternatives = strategy.findAlternatives("OPO", "LIS");

        assertTrue(alternatives.isEmpty());
    }
}