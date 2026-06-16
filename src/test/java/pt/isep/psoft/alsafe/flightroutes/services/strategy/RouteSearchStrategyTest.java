package pt.isep.psoft.alsafe.flightroutes.services.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteSearchStrategyTest {

    @Mock private FlightRouteRepository repository;

    private SearchByOriginStrategy byOrigin;
    private SearchByDestinationStrategy byDestination;
    private SearchByBothStrategy byBoth;
    private SearchAllStrategy searchAll;

    @BeforeEach
    void setUp() {
        byOrigin      = new SearchByOriginStrategy(repository);
        byDestination = new SearchByDestinationStrategy(repository);
        byBoth        = new SearchByBothStrategy(repository);
        searchAll     = new SearchAllStrategy(repository);
    }


    @Test
    void ensureByOriginSupports_OriginOnly() {
        assertTrue(byOrigin.supports("OPO", null));   
    }

    @Test
    void ensureByOriginDoesNotSupport_BothProvided() {
        assertFalse(byOrigin.supports("OPO", "LIS"));    
    }

    @Test
    void ensureByOriginDoesNotSupport_OriginNull() {
        assertFalse(byOrigin.supports(null, null));  
    }

    @Test
    void ensureByOriginExecuteDelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<FlightRoute> expected = new PageImpl<>(List.of());
        when(repository.findByOrigin_IataCode_Code("OPO", pageable)).thenReturn(expected);

        assertSame(expected, byOrigin.execute("OPO", null, pageable));
        verify(repository).findByOrigin_IataCode_Code("OPO", pageable);
    }

    @Test
    void ensureByDestinationSupports_DestinationOnly() {
        assertTrue(byDestination.supports(null, "LIS")); 
    }

    @Test
    void ensureByDestinationDoesNotSupport_OriginProvided() {
        assertFalse(byDestination.supports("OPO", "LIS")); 
    }

    @Test
    void ensureByDestinationDoesNotSupport_DestinationNull() {
        assertFalse(byDestination.supports(null, null)); 
    }

    @Test
    void ensureByDestinationExecuteDelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<FlightRoute> expected = new PageImpl<>(List.of());
        when(repository.findByDestination_IataCode_Code("LIS", pageable)).thenReturn(expected);

        assertSame(expected, byDestination.execute(null, "LIS", pageable));
        verify(repository).findByDestination_IataCode_Code("LIS", pageable);
    }

    @Test
    void ensureByBothSupports_BothProvided() {
        assertTrue(byBoth.supports("OPO", "LIS"));
    }

    @Test
    void ensureByBothDoesNotSupport_OriginNull() {
        assertFalse(byBoth.supports(null, "LIS"));
    }

    @Test
    void ensureByBothDoesNotSupport_DestinationNull() {
        assertFalse(byBoth.supports("OPO", null));
    }

    @Test
    void ensureByBothExecuteDelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<FlightRoute> expected = new PageImpl<>(List.of());
        when(repository.findByOrigin_IataCode_CodeAndDestination_IataCode_Code("OPO", "LIS", pageable))
                .thenReturn(expected);

        assertSame(expected, byBoth.execute("OPO", "LIS", pageable));
        verify(repository).findByOrigin_IataCode_CodeAndDestination_IataCode_Code("OPO", "LIS", pageable);
    }

    @Test
    void ensureSearchAllAlwaysSupports() {
        assertTrue(searchAll.supports(null, null));
        assertTrue(searchAll.supports("OPO", null));
        assertTrue(searchAll.supports(null, "LIS"));
        assertTrue(searchAll.supports("OPO", "LIS"));
    }

    @Test
    void ensureSearchAllExecuteDelegatesToRepository() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<FlightRoute> expected = new PageImpl<>(List.of());
        when(repository.findAll(pageable)).thenReturn(expected);

        assertSame(expected, searchAll.execute(null, null, pageable));
        verify(repository).findAll(pageable);
    }
}