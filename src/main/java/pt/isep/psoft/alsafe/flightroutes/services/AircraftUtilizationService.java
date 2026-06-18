package pt.isep.psoft.alsafe.flightroutes.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.isep.psoft.alsafe.flightroutes.api.AircraftUtilizationDTO;
import pt.isep.psoft.alsafe.flightroutes.api.AircraftUtilizationPeriodDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;
import pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for US223 - Aircraft utilization rates over time.
 * Computes per-aircraft monthly utilization (flight hours and number of flights)
 * from completed/scheduled flights in ScheduledFlight.
 *
 * Placed in flightroutes.services because it queries ScheduledFlight data
 * without touching the aircraftmanagement aggregate directly.
 */
@Service
public class AircraftUtilizationService {

    private final ScheduledFlightRepository scheduledFlightRepository;

    public AircraftUtilizationService(ScheduledFlightRepository scheduledFlightRepository) {
        this.scheduledFlightRepository = scheduledFlightRepository;
    }

    /**
     * Returns utilization data over time for all aircraft in the fleet.
     */
    @Transactional(readOnly = true)
    public List<AircraftUtilizationDTO> getUtilizationForAllAircraft() {
        List<ScheduledFlight> flights =
                scheduledFlightRepository.findNonCancelledFlightsForUtilization(null);
        return buildUtilizationList(flights);
    }

    /**
     * Returns utilization data over time for a single aircraft.
     *
     * @param registrationNumber the aircraft registration number (case-insensitive)
     */
    @Transactional(readOnly = true)
    public AircraftUtilizationDTO getUtilizationForAircraft(String registrationNumber) {
        String reg = registrationNumber.toUpperCase();
        List<ScheduledFlight> flights =
                scheduledFlightRepository.findNonCancelledFlightsForUtilization(reg);

        if (flights.isEmpty()) {

            throw new pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException("Aircraft with registration " + reg + " not found or has no utilization.");
        }

        List<AircraftUtilizationDTO> result = buildUtilizationList(flights);

        return result.get(0);
    }





    /**
     * Groups a list of ScheduledFlight entries by aircraft and then by year/month,
     * computing total flights and total flight hours for each period.
     *
     * Key structure: registrationNumber -> "YYYY-MM" -> [totalFlights, totalHours]
     */
    private List<AircraftUtilizationDTO> buildUtilizationList(List<ScheduledFlight> flights) {



        Map<String, AircraftMeta> aircraftMap = new LinkedHashMap<>();

        for (ScheduledFlight sf : flights) {
            String reg = sf.getAircraft().getRegistrationNumber();
            String modelName = sf.getAircraft().getModel().getModelName();

            AircraftMeta meta = aircraftMap.computeIfAbsent(reg, k -> new AircraftMeta(reg, modelName));

            int year = sf.getScheduledDeparture().getYear();
            int month = sf.getScheduledDeparture().getMonthValue();
            String periodKey = year + "-" + String.format("%02d", month);

            double flightHours = computeFlightHours(sf);
            meta.addFlight(periodKey, year, month, flightHours);
        }

        List<AircraftUtilizationDTO> result = new ArrayList<>();
        for (AircraftMeta meta : aircraftMap.values()) {
            result.add(meta.toDTO());
        }
        return result;
    }

    /**
     * Computes the duration of a scheduled flight in fractional hours.
     * Uses ChronoUnit.MINUTES for precision, then converts to hours.
     */
    private double computeFlightHours(ScheduledFlight sf) {
        long minutes = ChronoUnit.MINUTES.between(sf.getScheduledDeparture(), sf.getScheduledArrival());
        return minutes / 60.0;
    }





    /**
     * Accumulates per-period data for a single aircraft.
     */
    private static class AircraftMeta {
        private final String registrationNumber;
        private final String modelName;

        private final Map<String, PeriodAccumulator> periods = new LinkedHashMap<>();

        AircraftMeta(String registrationNumber, String modelName) {
            this.registrationNumber = registrationNumber;
            this.modelName = modelName;
        }

        void addFlight(String periodKey, int year, int month, double hours) {
            periods.computeIfAbsent(periodKey, k -> new PeriodAccumulator(year, month))
                   .add(hours);
        }

        AircraftUtilizationDTO toDTO() {
            List<AircraftUtilizationPeriodDTO> periodList = new ArrayList<>();
            for (PeriodAccumulator acc : periods.values()) {
                periodList.add(new AircraftUtilizationPeriodDTO(
                        acc.year, acc.month, acc.totalFlights, acc.totalHours));
            }
            return new AircraftUtilizationDTO(registrationNumber, modelName, periodList);
        }
    }

    private static class PeriodAccumulator {
        final int year;
        final int month;
        long totalFlights = 0;
        double totalHours = 0.0;

        PeriodAccumulator(int year, int month) {
            this.year = year;
            this.month = month;
        }

        void add(double hours) {
            totalFlights++;
            totalHours += hours;
        }
    }
}