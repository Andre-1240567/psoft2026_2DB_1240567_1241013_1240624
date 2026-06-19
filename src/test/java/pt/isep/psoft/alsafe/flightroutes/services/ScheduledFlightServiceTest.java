package pt.isep.psoft.alsafe.flightroutes.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.repositories.AirportRepository;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduledFlightServiceTest {

    @Mock
    private FlightRouteRepository flightRouteRepository;
    @Mock
    private AircraftRepository aircraftRepository;
    @Mock
    private ScheduledFlightRepository scheduledFlightRepository;
    @Mock
    private AirportRepository airportRepository;

    @InjectMocks
    private ScheduledFlightService scheduledFlightService;

    private FlightRoute activeRoute;
    private Aircraft availableAircraft;
    private Airport origin;
    private Airport dest;

    @BeforeEach
    void setUp() {
        origin = new Airport(new IATACode("OPO"), "OPO", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        origin.addCertification("Boeing 737");

        dest = new Airport(new IATACode("MAD"), "MAD", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        dest.addCertification("Boeing 737");

        RouteRequirement req = new RouteRequirement(1000.0, 100);
        activeRoute = new FlightRoute("route123", origin, dest, 500.0, 60, req, "atcc");

        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "Boeing 737", 150, 10000.0, 5000.0, 800.0);
        availableAircraft = new Aircraft("CS-TPA", model, LocalDate.now().minusYears(1), "Economy");
    }

    @Test
    void ensureScheduleFlightCreatesSuccessfully() {
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);

        when(flightRouteRepository.findById("route123")).thenReturn(Optional.of(activeRoute));
        when(aircraftRepository.findById("CS-TPA")).thenReturn(Optional.of(availableAircraft));
        
        when(scheduledFlightRepository.findOverlappingFlightsWithLock(any(), any(), any())).thenReturn(Collections.emptyList());
        
        when(scheduledFlightRepository.save(any(ScheduledFlight.class))).thenAnswer(i -> i.getArguments()[0]);

        ScheduledFlight flight = scheduledFlightService.scheduleFlight("route123", "CS-TPA", departure, arrival);

        assertNotNull(flight);
        assertEquals("CS-TPA", flight.getAircraft().getRegistrationNumber());
        verify(scheduledFlightRepository, times(1)).save(any(ScheduledFlight.class));
    }

    @Test
    void ensureScheduleFlightThrowsWhenArrivalBeforeDeparture() {
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.minusHours(2); 

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                scheduledFlightService.scheduleFlight("route123", "CS-TPA", departure, arrival));

        assertEquals("Arrival time must be after departure time.", ex.getMessage());
    }

    @Test
    void ensureScheduleFlightThrowsWhenRouteDeactivated() {
        activeRoute.deactivate("atcc");

        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);

        when(flightRouteRepository.findById("route123")).thenReturn(Optional.of(activeRoute));
        when(aircraftRepository.findById("CS-TPA")).thenReturn(Optional.of(availableAircraft));

        assertThrows(IllegalStateException.class, () ->
                scheduledFlightService.scheduleFlight("route123", "CS-TPA", departure, arrival));
    }

    @Test
    void ensureScheduleFlightThrowsWhenAirportsNotCertified() {
        AircraftModel a380 = new AircraftModel(Manufacturer.AIRBUS, "A380", 500, 20000.0, 10000.0, 900.0);
        Aircraft uncertifiedAircraft = new Aircraft("CS-A380", a380, LocalDate.now().minusYears(1), "Economy");

        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);

        when(flightRouteRepository.findById("route123")).thenReturn(Optional.of(activeRoute));
        when(aircraftRepository.findById("CS-A380")).thenReturn(Optional.of(uncertifiedAircraft));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                scheduledFlightService.scheduleFlight("route123", "CS-A380", departure, arrival));

        assertTrue(ex.getMessage().contains("is not certified for aircraft model"));
    }

    @Test
    void ensureScheduleFlightThrowsWhenTimeOverlaps() {
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);

        when(flightRouteRepository.findById("route123")).thenReturn(Optional.of(activeRoute));
        when(aircraftRepository.findById("CS-TPA")).thenReturn(Optional.of(availableAircraft));
        
        ScheduledFlight dummyConflict = new ScheduledFlight(activeRoute, availableAircraft, departure.minusMinutes(10), arrival.minusMinutes(10));
        when(scheduledFlightRepository.findOverlappingFlightsWithLock(any(), any(), any())).thenReturn(List.of(dummyConflict));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                scheduledFlightService.scheduleFlight("route123", "CS-TPA", departure, arrival));

        assertTrue(ex.getMessage().contains("already scheduled"));
    }

    @Test
    void ensureGetFlightsByAircraftThrowsWhenAircraftNotFound() {
        when(aircraftRepository.existsById("UNKNOWN")).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () ->
                scheduledFlightService.getScheduledFlightsByAircraft("UNKNOWN"));
    }

    @Test
    void ensureCancelFlightWorksSuccessfully() {
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(activeRoute, availableAircraft, departure, arrival);
        
        when(scheduledFlightRepository.findById("FL123")).thenReturn(Optional.of(flight));
        when(scheduledFlightRepository.save(any(ScheduledFlight.class))).thenAnswer(i -> i.getArguments()[0]);

        ScheduledFlight canceled = scheduledFlightService.cancelFlight("FL123");

        assertEquals("CANCELED", canceled.getStatus().name());
        verify(scheduledFlightRepository).save(flight);
    }

    @Test
    void ensureCancelFlightThrowsWhenNotFound() {
        when(scheduledFlightRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> scheduledFlightService.cancelFlight("nonexistent"));
    }

    @Test
    void ensureGetFlightByIdWorks() {
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(activeRoute, availableAircraft, departure, arrival);
        
        when(scheduledFlightRepository.findById("FL123")).thenReturn(Optional.of(flight));

        ScheduledFlight result = scheduledFlightService.getFlightById("FL123");

        assertNotNull(result);
        assertEquals(flight, result);
    }

    @Test
    void ensureGetFlightByIdThrowsWhenNotFound() {
        when(scheduledFlightRepository.findById("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> scheduledFlightService.getFlightById("nonexistent"));
    }

    @Test
    void ensureGetUpcomingDeparturesWorks() {
        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(activeRoute, availableAircraft, departure, arrival);
        
        when(airportRepository.findByIataCode_Code("OPO")).thenReturn(Optional.of(origin));
        when(scheduledFlightRepository.findUpcomingDepartures(eq("OPO"), any(), any())).thenReturn(List.of(flight));

        List<ScheduledFlight> departures = scheduledFlightService.getUpcomingDepartures("OPO", 24);

        assertEquals(1, departures.size());
        assertEquals(flight, departures.get(0));
    }

    @Test
    void ensureGetUpcomingDeparturesThrowsWhenHoursInvalid() {
        assertThrows(IllegalArgumentException.class, () -> scheduledFlightService.getUpcomingDepartures("OPO", 0));
        assertThrows(IllegalArgumentException.class, () -> scheduledFlightService.getUpcomingDepartures("OPO", -5));
    }

    @Test
    void ensureGetUpcomingDeparturesThrowsWhenAirportNotFound() {
        when(airportRepository.findByIataCode_Code("XXX")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> scheduledFlightService.getUpcomingDepartures("XXX", 24));
    }

    @Test
    void ensureScheduleFlightThrowsWhenRouteNotFound() {
        when(flightRouteRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> 
                scheduledFlightService.scheduleFlight("nonexistent", "CS-TPA", LocalDateTime.now(), LocalDateTime.now().plusHours(2)));
    }

    @Test
    void ensureScheduleFlightThrowsWhenAircraftNotFound() {
        when(flightRouteRepository.findById("route123")).thenReturn(Optional.of(activeRoute));
        when(aircraftRepository.findById("nonexistent")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> 
                scheduledFlightService.scheduleFlight("route123", "nonexistent", LocalDateTime.now(), LocalDateTime.now().plusHours(2)));
    }

    @Test
    void ensureGetScheduledFlightsByAircraftReturnsListSuccessfully() {
        when(aircraftRepository.existsById("CS-TPA")).thenReturn(true);
        when(scheduledFlightRepository.findByAircraft_RegistrationNumber("CS-TPA"))
                .thenReturn(List.of(new ScheduledFlight(activeRoute, availableAircraft, LocalDateTime.now(), LocalDateTime.now().plusHours(2))));
        
        List<ScheduledFlight> flights = scheduledFlightService.getScheduledFlightsByAircraft("CS-TPA");
        
        assertFalse(flights.isEmpty());
        verify(scheduledFlightRepository).findByAircraft_RegistrationNumber("CS-TPA");
    }

    @Test
    void ensureScheduleFlightThrowsWhenDestinationNotCertified() {
        Airport newDest = new Airport(new IATACode("LIS"), "LIS", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        
        FlightRoute routeToLis = new FlightRoute("routeLIS", origin, newDest, 500.0, 60, new RouteRequirement(1000.0, 100), "atcc");
        
        when(flightRouteRepository.findById("routeLIS")).thenReturn(Optional.of(routeToLis));
        when(aircraftRepository.findById("CS-TPA")).thenReturn(Optional.of(availableAircraft));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                scheduledFlightService.scheduleFlight("routeLIS", "CS-TPA", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2)));
        
        assertTrue(ex.getMessage().contains("destination airport"));
    }

    @Test
    void ensureScheduleFlightThrowsWhenRangeInsufficient() {
        FlightRoute longRoute = new FlightRoute(
                "routeLong", origin, dest, 2000.0, 60,
                new RouteRequirement(2000.0, 100), "atcc");

       AircraftModel shortRangeModel = new AircraftModel(
                Manufacturer.BOEING, "Boeing 737", 150, 5000.0, 1000.0, 800.0);

        Aircraft shortRangeAircraft = spy(
                new Aircraft("CS-SR", shortRangeModel, LocalDate.now().minusYears(1), "Economy"));
        doReturn(AircraftStatus.AVAILABLE).when(shortRangeAircraft).getStatus();

        when(flightRouteRepository.findById("routeLong")).thenReturn(Optional.of(longRoute));
        when(aircraftRepository.findById("CS-SR")).thenReturn(Optional.of(shortRangeAircraft));
        when(scheduledFlightRepository.findOverlappingFlightsWithLock(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                scheduledFlightService.scheduleFlight(
                        "routeLong", "CS-SR",
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(2)));

        assertTrue(ex.getMessage().contains("maximum range is insufficient"));
    }

    @Test
    void ensureScheduleFlightThrowsWhenCapacityInsufficient() {
        AircraftModel smallModel = new AircraftModel(Manufacturer.BOEING, "Boeing 737", 50, 10000.0, 5000.0, 800.0);
        Aircraft smallAircraft = new Aircraft("CS-SM", smallModel, LocalDate.now().minusYears(1), "Economy");
        
        when(flightRouteRepository.findById("route123")).thenReturn(Optional.of(activeRoute));
        when(aircraftRepository.findById("CS-SM")).thenReturn(Optional.of(smallAircraft));
        
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                scheduledFlightService.scheduleFlight("route123", "CS-SM", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2)));
        
        assertTrue(ex.getMessage().contains("active capacity is insufficient"));
    }

    @Test
    void ensureScheduleFlightThrowsWhenAircraftNotAvailable() {
        Aircraft unavailableAircraft = spy(availableAircraft);
        when(unavailableAircraft.getStatus()).thenReturn(null); 
        
        when(flightRouteRepository.findById("route123")).thenReturn(Optional.of(activeRoute));
        when(aircraftRepository.findById("CS-UN")).thenReturn(Optional.of(unavailableAircraft));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                scheduledFlightService.scheduleFlight("route123", "CS-UN", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2)));
        
        assertTrue(ex.getMessage().contains("not available for scheduling"));
    }

    @Test
    void ensureScheduleFlightThrowsWhenDestinationClosed() {
        Airport destClosed = new Airport(new IATACode("LIS"), "LIS", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));        
        destClosed.changeStatus(Status.CLOSED);
        
        FlightRoute routeToClosed = new FlightRoute("routeClosed", origin, destClosed, 500.0, 60, new RouteRequirement(1000.0, 100), "atcc");
        
        when(flightRouteRepository.findById("routeClosed")).thenReturn(Optional.of(routeToClosed));
        when(aircraftRepository.findById("CS-TPA")).thenReturn(Optional.of(availableAircraft));
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                scheduledFlightService.scheduleFlight("routeClosed", "CS-TPA", LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2)));
        
        assertTrue(ex.getMessage().contains("must be operational"));
    }
    @Test
    void ensureScheduleFlightThrowsWhenOriginClosed() {
       Airport closedOrigin = new Airport(
                new IATACode("LIS"), "LIS",
                new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)),
                new Timezone("UTC+00:00"));
        closedOrigin.changeStatus(Status.CLOSED);
        closedOrigin.addCertification("Boeing 737");

        FlightRoute routeWithClosedOrigin = new FlightRoute(
                "routeClosedOrigin", closedOrigin, dest, 500.0, 60,
                new RouteRequirement(1000.0, 100), "atcc");

        when(flightRouteRepository.findById("routeClosedOrigin"))
                .thenReturn(Optional.of(routeWithClosedOrigin));
        when(aircraftRepository.findById("CS-TPA"))
                .thenReturn(Optional.of(availableAircraft));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                scheduledFlightService.scheduleFlight(
                        "routeClosedOrigin", "CS-TPA",
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(2)));

        assertTrue(ex.getMessage().contains("must be operational"));
    }
    
}
