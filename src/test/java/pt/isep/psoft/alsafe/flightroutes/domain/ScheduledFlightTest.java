package pt.isep.psoft.alsafe.flightroutes.domain;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ScheduledFlightTest {

    @Test
    void ensureScheduledFlightIsCreatedWithValidData() {
        FlightRoute mockRoute = mock(FlightRoute.class);
        Aircraft mockAircraft = mock(Aircraft.class);
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);

        ScheduledFlight flight = new ScheduledFlight(mockRoute, mockAircraft, departure, arrival);

        assertNotNull(flight.getFlightNumber());
        assertEquals(FlightStatus.SCHEDULED, flight.getStatus());
        assertEquals(departure, flight.getScheduledDeparture());
        assertEquals(arrival, flight.getScheduledArrival());
    }

    @Test
    void ensureExceptionIsThrownIfArrivalIsBeforeDeparture() {
        FlightRoute mockRoute = mock(FlightRoute.class);
        Aircraft mockAircraft = mock(Aircraft.class);
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.minusHours(2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new ScheduledFlight(mockRoute, mockAircraft, departure, arrival);
        });
        
        assertEquals("Arrival time must be after departure time.", exception.getMessage());
    }

    @Test
    void ensureFlightIsCanceledSuccessfully() {
        FlightRoute mockRoute = mock(FlightRoute.class);
        Aircraft mockAircraft = mock(Aircraft.class);
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(mockRoute, mockAircraft, departure, arrival);

        flight.cancel();

        assertEquals(FlightStatus.CANCELED, flight.getStatus());
    }

    @Test
    void ensureExceptionIsThrownWhenCancelingAlreadyCanceledFlight() {
        FlightRoute mockRoute = mock(FlightRoute.class);
        Aircraft mockAircraft = mock(Aircraft.class);
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(mockRoute, mockAircraft, departure, arrival);
        
        flight.cancel();

        IllegalStateException exception = assertThrows(IllegalStateException.class, flight::cancel);
        assertEquals("This flight is already canceled.", exception.getMessage());
    }

    @Test
    void ensureProtectedConstructorCreatesInstanceForJpa() {
        assertDoesNotThrow(() -> {
            java.lang.reflect.Constructor<ScheduledFlight> constructor =
                    ScheduledFlight.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            ScheduledFlight flight = constructor.newInstance();
            assertNotNull(flight);
        });
    }

    @Test
    void ensureExceptionIsThrownWhenRouteIsNull() {
        Aircraft mockAircraft = mock(Aircraft.class);
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ScheduledFlight(null, mockAircraft, departure, arrival));

        assertEquals("Flight route cannot be null.", ex.getMessage());
    }

    @Test
    void ensureExceptionIsThrownWhenAircraftIsNull() {
        FlightRoute mockRoute = mock(FlightRoute.class);
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ScheduledFlight(mockRoute, null, departure, arrival));

        assertEquals("Aircraft cannot be null.", ex.getMessage());
    }

    @Test
    void ensureExceptionIsThrownWhenDepartureIsNull() {
        FlightRoute mockRoute = mock(FlightRoute.class);
        Aircraft mockAircraft = mock(Aircraft.class);
        LocalDateTime arrival = LocalDateTime.now().plusDays(1).plusHours(2);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ScheduledFlight(mockRoute, mockAircraft, null, arrival));

        assertEquals("Departure and arrival times must be provided.", ex.getMessage());
    }

    @Test
    void ensureExceptionIsThrownWhenArrivalIsNull() {
        FlightRoute mockRoute = mock(FlightRoute.class);
        Aircraft mockAircraft = mock(Aircraft.class);
        LocalDateTime departure = LocalDateTime.now().plusDays(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ScheduledFlight(mockRoute, mockAircraft, departure, null));

        assertEquals("Departure and arrival times must be provided.", ex.getMessage());
    }

    @Test
    void ensureExceptionIsThrownWhenArrivalIsEqualToDeparture() {
        FlightRoute mockRoute = mock(FlightRoute.class);
        Aircraft mockAircraft = mock(Aircraft.class);
        LocalDateTime departure = LocalDateTime.now().plusDays(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ScheduledFlight(mockRoute, mockAircraft, departure, departure));

        assertEquals("Arrival time must be after departure time.", ex.getMessage());
    }

    @Test
    void ensureExceptionIsThrownWhenCancelingCompletedFlight() {
        FlightRoute mockRoute = mock(FlightRoute.class);
        Aircraft mockAircraft = mock(Aircraft.class);
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(mockRoute, mockAircraft, departure, arrival);

        assertDoesNotThrow(() -> {
            java.lang.reflect.Field statusField = ScheduledFlight.class.getDeclaredField("status");
            statusField.setAccessible(true);
            statusField.set(flight, FlightStatus.COMPLETED);
        });

        IllegalStateException ex = assertThrows(IllegalStateException.class, flight::cancel);
        assertEquals("Cannot cancel a completed flight.", ex.getMessage());
    }
}