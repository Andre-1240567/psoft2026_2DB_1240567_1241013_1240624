package pt.isep.psoft.alsafe.flightroutes.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledFlightService {

    private final FlightRouteRepository flightRouteRepository;
    private final AircraftRepository aircraftRepository;
    private final ScheduledFlightRepository scheduledFlightRepository;
    @Transactional
    public ScheduledFlight scheduleFlight(String routeId, String aircraftRegistration, LocalDateTime departureTime, LocalDateTime arrivalTime) {
        
        FlightRoute route = flightRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight Route not found with ID: " + routeId));

        Aircraft aircraft = aircraftRepository.findById(aircraftRegistration)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft not found with registration: " + aircraftRegistration));

        if (!route.getOrigin().getStatus().name().equals("OPERATIONAL") || 
            !route.getDestination().getStatus().name().equals("OPERATIONAL")) {
            throw new IllegalArgumentException("Both origin and destination airports must be operational.");
        }

        if (aircraft.getModel().getMaxRange() < route.getDistance()) {
            throw new IllegalArgumentException("Aircraft maximum range is insufficient for this route.");
        }

        if (aircraft.getStatus() != AircraftStatus.AVAILABLE) {
            throw new IllegalArgumentException("Aircraft is not available. Current status: " + aircraft.getStatus());
        }

        boolean isOverlapping = scheduledFlightRepository.existsByAircraftAndTimeRange(aircraft, departureTime, arrivalTime);
        if (isOverlapping) {
            throw new IllegalStateException("The aircraft is already scheduled for another flight during this timeframe.");
        }

        ScheduledFlight newFlight = new ScheduledFlight(route, aircraft, departureTime, arrivalTime);
        return scheduledFlightRepository.save(newFlight);
    }

    public List<ScheduledFlight> getScheduledFlightsByAircraft(String aircraftRegistration) {
        if (!aircraftRepository.existsById(aircraftRegistration)) {
            throw new ResourceNotFoundException("Aircraft not found with registration: " + aircraftRegistration);
        }
        return scheduledFlightRepository.findByAircraft_RegistrationNumber(aircraftRegistration);
    }
}