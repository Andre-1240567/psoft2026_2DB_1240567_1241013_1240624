package pt.isep.psoft.alsafe.flightroutes.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.Status;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteStatus;
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

    private static final int TURNAROUND_BUFFER_MINUTES = 30;

    @Transactional
    public ScheduledFlight scheduleFlight(String routeId, String aircraftRegistration,
                                          LocalDateTime departureTime, LocalDateTime arrivalTime) {

        if (!arrivalTime.isAfter(departureTime)) {
            throw new IllegalArgumentException("Arrival time must be after departure time.");
        }

        FlightRoute route = flightRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight Route not found with ID: " + routeId));

        Aircraft aircraft = aircraftRepository.findById(aircraftRegistration)
                .orElseThrow(() -> new ResourceNotFoundException("Aircraft not found with registration: " + aircraftRegistration));

        if (route.getRouteStatus() != RouteStatus.ACTIVE) {
            throw new IllegalStateException("Cannot schedule a flight on a deactivated route.");
        }

        Airport origin = route.getOrigin();
        Airport destination = route.getDestination();

        if (origin.getStatus() != Status.OPERATIONAL || destination.getStatus() != Status.OPERATIONAL) {
            throw new IllegalStateException("Both origin and destination airports must be operational.");
        }

        String modelName = aircraft.getModel().getModelName();

        boolean originCertified = origin.getCertifications().stream()
                .anyMatch(cert -> cert.getModelName().equals(modelName));

        if (!originCertified) {
            throw new IllegalStateException(
                    "The origin airport (" + origin.getIataCode().getCode() +
                    ") is not certified for aircraft model: " + modelName);
        }

        boolean destinationCertified = destination.getCertifications().stream()
                .anyMatch(cert -> cert.getModelName().equals(modelName));

        if (!destinationCertified) {
            throw new IllegalStateException(
                    "The destination airport (" + destination.getIataCode().getCode() +
                    ") is not certified for aircraft model: " + modelName);
        }

        if (aircraft.getModel().getMaxRange() < route.getDistance()) {
            throw new IllegalArgumentException("Aircraft maximum range is insufficient for this route.");
        }

        if (aircraft.getActiveCapacity() < route.getRouteRequirement().getMinCapacityRequired()) {
            throw new IllegalArgumentException("Aircraft active capacity is insufficient for this route's requirements.");
        }

        if (aircraft.getStatus() != AircraftStatus.AVAILABLE) {
            throw new IllegalStateException("Aircraft is not available for scheduling. Current status: " + aircraft.getStatus());
        }

        LocalDateTime bufferedDeparture = departureTime.minusMinutes(TURNAROUND_BUFFER_MINUTES);
        LocalDateTime bufferedArrival = arrivalTime.plusMinutes(TURNAROUND_BUFFER_MINUTES);

        List<ScheduledFlight> overlaps = scheduledFlightRepository.findOverlappingFlightsWithLock(
                aircraft, bufferedDeparture, bufferedArrival);

        if (!overlaps.isEmpty()) {
            throw new IllegalStateException("The aircraft is already scheduled...");
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

    public ScheduledFlight getFlightById(String flightNumber) {
        return scheduledFlightRepository.findById(flightNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Scheduled flight not found with number: " + flightNumber));
    }
}