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
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
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

    @InjectMocks
    private ScheduledFlightService scheduledFlightService;

    private FlightRoute activeRoute;
    private Aircraft availableAircraft;
    private Airport origin;
    private Airport dest;

    @BeforeEach
    void setUp() {
        origin = new Airport(new IATACode("OPO"), "OPO", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        origin.changeStatus(Status.OPERATIONAL);
        origin.addCertification("Boeing 737");

        dest = new Airport(new IATACode("MAD"), "MAD", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        dest.changeStatus(Status.OPERATIONAL);
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
        
        // CORREÇÃO: Fazer mock do novo método devolvendo uma lista vazia (sem colisão)
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
        
        // CORREÇÃO: Fazer mock do novo método simulando uma colisão (devolve uma lista com 1 elemento)
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
}