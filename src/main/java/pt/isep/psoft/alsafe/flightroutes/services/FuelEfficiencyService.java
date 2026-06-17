package pt.isep.psoft.alsafe.flightroutes.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import pt.isep.psoft.alsafe.flightroutes.api.AircraftFuelEfficiencyDTO;
import pt.isep.psoft.alsafe.flightroutes.api.RouteFuelEfficiencyDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for US227 - Fuel efficiency metrics per aircraft and per route.
 *
 * Design decision: fuel burn rate is derived from AircraftModel specifications:
 *   fuelBurnRate (L/km) = fuelCapacity / maxRange
 * This represents the average consumption over the model's certified maximum range.
 * No dedicated fuelBurnPerKm field exists in the domain — this derivation avoids
 * modifying the aircraftmanagement aggregate (owned by a different team member)
 * while still producing meaningful and consistent metrics.
 */
@Service
@RequiredArgsConstructor
public class FuelEfficiencyService {

    private final ScheduledFlightRepository scheduledFlightRepository;
    private final FlightRouteRepository     flightRouteRepository;
    private final AircraftRepository        aircraftRepository;

    @Transactional(readOnly = true)
    public List<AircraftFuelEfficiencyDTO> getEfficiencyForAllAircraft() {
        List<ScheduledFlight> flights =
                scheduledFlightRepository.findNonCancelledFlightsForUtilization(null);
        return buildAircraftEfficiencyList(flights);
    }

    @Transactional(readOnly = true)
    public AircraftFuelEfficiencyDTO getEfficiencyForAircraft(String registrationNumber) {
        String reg = registrationNumber.toUpperCase();

        if (!aircraftRepository.existsById(reg)) {
            throw new ResourceNotFoundException(
                    "Aircraft not found with registration: " + reg);
        }

        List<ScheduledFlight> flights =
                scheduledFlightRepository.findNonCancelledFlightsForUtilization(reg);

        if (flights.isEmpty()) {
            return aircraftRepository.findById(reg)
                    .map(a -> zeroAircraftDTO(
                            a.getRegistrationNumber(),
                            a.getModel().getModelName(),
                            fuelBurnRate(a.getModel())))
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Aircraft not found with registration: " + reg));
        }

        List<AircraftFuelEfficiencyDTO> result = buildAircraftEfficiencyList(flights);
        return result.get(0);
    }

    @Transactional(readOnly = true)
    public List<RouteFuelEfficiencyDTO> getEfficiencyForAllRoutes() {
        List<ScheduledFlight> flights =
                scheduledFlightRepository.findNonCancelledFlightsForUtilization(null);
        return buildRouteEfficiencyList(flights);
    }

    @Transactional(readOnly = true)
    public RouteFuelEfficiencyDTO getEfficiencyForRoute(String routeId) {
        FlightRoute route = flightRouteRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Flight route not found with ID: " + routeId));

        List<ScheduledFlight> flights =
                scheduledFlightRepository.findNonCancelledFlightsForUtilization(null)
                        .stream()
                        .filter(sf -> sf.getRoute().getRouteIdValue().equals(routeId))
                        .toList();

        if (flights.isEmpty()) {
            return zeroRouteDTO(route);
        }

        List<RouteFuelEfficiencyDTO> result = buildRouteEfficiencyList(flights);
        return result.get(0);
    }

    private List<AircraftFuelEfficiencyDTO> buildAircraftEfficiencyList(
            List<ScheduledFlight> flights) {

        Map<String, AircraftAccumulator> map = new LinkedHashMap<>();

        for (ScheduledFlight sf : flights) {
            String reg       = sf.getAircraft().getRegistrationNumber();
            String modelName = sf.getAircraft().getModel().getModelName();
            double burnRate  = fuelBurnRate(sf.getAircraft().getModel());
            double distance  = sf.getRoute().getDistance();

            map.computeIfAbsent(reg, k -> new AircraftAccumulator(reg, modelName, burnRate))
               .add(distance);
        }

        List<AircraftFuelEfficiencyDTO> result = new ArrayList<>();
        for (AircraftAccumulator acc : map.values()) {
            result.add(acc.toDTO());
        }
        return result;
    }

    private List<RouteFuelEfficiencyDTO> buildRouteEfficiencyList(
            List<ScheduledFlight> flights) {

        Map<String, RouteAccumulator> map = new LinkedHashMap<>();

        for (ScheduledFlight sf : flights) {
            FlightRoute route    = sf.getRoute();
            String      routeId  = route.getRouteIdValue();
            double      burnRate = fuelBurnRate(sf.getAircraft().getModel());
            double      distance = route.getDistance();

            map.computeIfAbsent(routeId, k -> new RouteAccumulator(
                    routeId,
                    route.getOrigin().getIataCode().getCode(),
                    route.getDestination().getIataCode().getCode(),
                    distance))
               .add(burnRate);
        }

        List<RouteFuelEfficiencyDTO> result = new ArrayList<>();
        for (RouteAccumulator acc : map.values()) {
            result.add(acc.toDTO());
        }
        return result;
    }

    private double fuelBurnRate(AircraftModel model) {
        return model.getFuelCapacity() / model.getMaxRange();
    }

    private AircraftFuelEfficiencyDTO zeroAircraftDTO(String reg, String modelName,
                                                       double burnRate) {
        return new AircraftFuelEfficiencyDTO(reg, modelName, burnRate, 0, 0,
                burnRate > 0 ? 1.0 / burnRate : 0, 0);
    }

    private RouteFuelEfficiencyDTO zeroRouteDTO(FlightRoute route) {
        return new RouteFuelEfficiencyDTO(
                route.getRouteIdValue(),
                route.getOrigin().getIataCode().getCode(),
                route.getDestination().getIataCode().getCode(),
                route.getDistance(),
                0, 0, 0);
    }

    private static class AircraftAccumulator {
        final String registrationNumber;
        final String modelName;
        final double burnRate;
        double totalDistance = 0;
        int    flightCount   = 0;

        AircraftAccumulator(String registrationNumber, String modelName, double burnRate) {
            this.registrationNumber = registrationNumber;
            this.modelName          = modelName;
            this.burnRate           = burnRate;
        }

        void add(double distanceKm) {
            totalDistance += distanceKm;
            flightCount++;
        }

        AircraftFuelEfficiencyDTO toDTO() {
            double totalFuel     = burnRate * totalDistance;
            double efficiencyKmL = burnRate > 0 ? 1.0 / burnRate : 0;
            return new AircraftFuelEfficiencyDTO(
                    registrationNumber, modelName,
                    burnRate, totalDistance, totalFuel,
                    efficiencyKmL, flightCount);
        }
    }

    private static class RouteAccumulator {
        final String routeId;
        final String originIata;
        final String destinationIata;
        final double distanceKm;
        double totalBurnRate = 0;
        int    flightCount   = 0;

        RouteAccumulator(String routeId, String originIata,
                         String destinationIata, double distanceKm) {
            this.routeId         = routeId;
            this.originIata      = originIata;
            this.destinationIata = destinationIata;
            this.distanceKm      = distanceKm;
        }

        void add(double burnRate) {
            totalBurnRate += burnRate;
            flightCount++;
        }

        RouteFuelEfficiencyDTO toDTO() {
            double avgBurnRate            = totalBurnRate / flightCount;
            double estimatedFuelPerFlight = avgBurnRate * distanceKm;
            double efficiencyKmL          = avgBurnRate > 0 ? 1.0 / avgBurnRate : 0;
            return new RouteFuelEfficiencyDTO(
                    routeId, originIata, destinationIata,
                    distanceKm, estimatedFuelPerFlight,
                    efficiencyKmL, flightCount);
        }
    }
}