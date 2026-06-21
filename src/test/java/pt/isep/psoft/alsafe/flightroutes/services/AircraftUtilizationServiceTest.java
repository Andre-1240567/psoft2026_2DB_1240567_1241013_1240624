package pt.isep.psoft.alsafe.flightroutes.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.flightroutes.api.dto.AircraftUtilizationDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;
import pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AircraftUtilizationServiceTest {

    private ScheduledFlightRepository scheduledFlightRepository;
    private AircraftUtilizationService service;

    private Aircraft aircraftA;
    private Aircraft aircraftB;
    private FlightRoute mockRoute;

    @BeforeEach
    void setUp() {
        scheduledFlightRepository = mock(ScheduledFlightRepository.class);
        service = new AircraftUtilizationService(scheduledFlightRepository);

        AircraftModel modelA = new AircraftModel(Manufacturer.BOEING, "737", 150, 10000.0, 5000.0, 800.0);
        AircraftModel modelB = new AircraftModel(Manufacturer.AIRBUS, "A320", 180, 12000.0, 6000.0, 850.0);

        aircraftA = new Aircraft("CS-TUA", modelA, LocalDate.now().minusYears(2), "Economy");
        aircraftB = new Aircraft("CS-TUB", modelB, LocalDate.now().minusYears(1), "Business");

        mockRoute = mock(FlightRoute.class);
    }

    @Test
    void ensureGetUtilizationForAllAircraftReturnsEmptyListWhenNoFlights() {
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of());

        List<AircraftUtilizationDTO> result = service.getUtilizationForAllAircraft();

        assertTrue(result.isEmpty());
        verify(scheduledFlightRepository).findNonCancelledFlightsForUtilization(null);
    }

    @Test
    void ensureGetUtilizationForAllAircraftGroupsFlightsByAircraft() {
        LocalDateTime dep1 = LocalDateTime.of(2025, 1, 10, 8, 0);
        LocalDateTime arr1 = dep1.plusHours(2);
        LocalDateTime dep2 = LocalDateTime.of(2025, 1, 15, 10, 0);
        LocalDateTime arr2 = dep2.plusHours(3);

        ScheduledFlight flightA1 = new ScheduledFlight(mockRoute, aircraftA, dep1, arr1);
        ScheduledFlight flightB1 = new ScheduledFlight(mockRoute, aircraftB, dep2, arr2);

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of(flightA1, flightB1));

        List<AircraftUtilizationDTO> result = service.getUtilizationForAllAircraft();

        assertEquals(2, result.size());
        AircraftUtilizationDTO dtoA = result.stream()
                .filter(d -> d.getRegistrationNumber().equals("CS-TUA"))
                .findFirst().orElseThrow();
        assertEquals("737", dtoA.getModelName());
        assertEquals(1, dtoA.getTotalFlights());
        assertEquals(2.0, dtoA.getTotalFlightHours(), 0.001);
    }

    @Test
    void ensureGetUtilizationForAllAircraftGroupsFlightsByMonthWithinSameAircraft() {
        LocalDateTime dep1 = LocalDateTime.of(2025, 1, 10, 8, 0);
        LocalDateTime arr1 = dep1.plusHours(2);
        LocalDateTime dep2 = LocalDateTime.of(2025, 1, 20, 8, 0);
        LocalDateTime arr2 = dep2.plusHours(3);
        LocalDateTime dep3 = LocalDateTime.of(2025, 2, 5, 8, 0);
        LocalDateTime arr3 = dep3.plusHours(1);

        ScheduledFlight f1 = new ScheduledFlight(mockRoute, aircraftA, dep1, arr1);
        ScheduledFlight f2 = new ScheduledFlight(mockRoute, aircraftA, dep2, arr2);
        ScheduledFlight f3 = new ScheduledFlight(mockRoute, aircraftA, dep3, arr3);

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of(f1, f2, f3));

        List<AircraftUtilizationDTO> result = service.getUtilizationForAllAircraft();

        assertEquals(1, result.size());
        AircraftUtilizationDTO dto = result.get(0);
        assertEquals(2, dto.getUtilizationByPeriod().size());

        assertEquals(2025, dto.getUtilizationByPeriod().get(0).getYear());
        assertEquals(1, dto.getUtilizationByPeriod().get(0).getMonth());
        assertEquals(2, dto.getUtilizationByPeriod().get(0).getTotalFlights());
        assertEquals(5.0, dto.getUtilizationByPeriod().get(0).getTotalFlightHours(), 0.001);

        assertEquals(2025, dto.getUtilizationByPeriod().get(1).getYear());
        assertEquals(2, dto.getUtilizationByPeriod().get(1).getMonth());
        assertEquals(1, dto.getUtilizationByPeriod().get(1).getTotalFlights());
        assertEquals(1.0, dto.getUtilizationByPeriod().get(1).getTotalFlightHours(), 0.001);
    }

    @Test
    void ensureGetUtilizationForAllAircraftSumsTotalsCorrectly() {
        LocalDateTime dep1 = LocalDateTime.of(2025, 3, 1, 8, 0);
        LocalDateTime arr1 = dep1.plusHours(4);
        LocalDateTime dep2 = LocalDateTime.of(2025, 3, 15, 8, 0);
        LocalDateTime arr2 = dep2.plusMinutes(90);

        ScheduledFlight f1 = new ScheduledFlight(mockRoute, aircraftA, dep1, arr1);
        ScheduledFlight f2 = new ScheduledFlight(mockRoute, aircraftA, dep2, arr2);

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of(f1, f2));

        List<AircraftUtilizationDTO> result = service.getUtilizationForAllAircraft();

        assertEquals(1, result.size());
        AircraftUtilizationDTO dto = result.get(0);
        assertEquals(2, dto.getTotalFlights());
        assertEquals(5.5, dto.getTotalFlightHours(), 0.001);
    }

    @Test
    void ensureGetUtilizationForAllAircraftHandlesFlightsAcrossMultipleYears() {
        LocalDateTime dep2024 = LocalDateTime.of(2024, 12, 20, 8, 0);
        LocalDateTime arr2024 = dep2024.plusHours(2);
        LocalDateTime dep2025 = LocalDateTime.of(2025, 1, 5, 8, 0);
        LocalDateTime arr2025 = dep2025.plusHours(3);

        ScheduledFlight f2024 = new ScheduledFlight(mockRoute, aircraftA, dep2024, arr2024);
        ScheduledFlight f2025 = new ScheduledFlight(mockRoute, aircraftA, dep2025, arr2025);

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of(f2024, f2025));

        List<AircraftUtilizationDTO> result = service.getUtilizationForAllAircraft();

        assertEquals(1, result.size());
        AircraftUtilizationDTO dto = result.get(0);
        assertEquals(2, dto.getUtilizationByPeriod().size());
        assertEquals(2024, dto.getUtilizationByPeriod().get(0).getYear());
        assertEquals(2025, dto.getUtilizationByPeriod().get(1).getYear());
    }

    @Test
    void ensureGetUtilizationForAircraftReturnsCorrectRegistration() {
        LocalDateTime dep = LocalDateTime.of(2025, 4, 10, 8, 0);
        LocalDateTime arr = dep.plusHours(2);
        ScheduledFlight flight = new ScheduledFlight(mockRoute, aircraftA, dep, arr);

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUA"))
                .thenReturn(List.of(flight));

        AircraftUtilizationDTO dto = service.getUtilizationForAircraft("CS-TUA");

        assertEquals("CS-TUA", dto.getRegistrationNumber());
        assertEquals("737", dto.getModelName());
        assertEquals(1, dto.getTotalFlights());
        assertEquals(2.0, dto.getTotalFlightHours(), 0.001);
    }

    @Test
    void ensureGetUtilizationForAircraftNormalizesRegistrationToUpperCase() {
        LocalDateTime dep = LocalDateTime.of(2025, 5, 1, 10, 0);
        LocalDateTime arr = dep.plusHours(1);
        ScheduledFlight flight = new ScheduledFlight(mockRoute, aircraftA, dep, arr);

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUA"))
                .thenReturn(List.of(flight));

        AircraftUtilizationDTO dto = service.getUtilizationForAircraft("cs-tua");

        assertEquals("CS-TUA", dto.getRegistrationNumber());
        verify(scheduledFlightRepository).findNonCancelledFlightsForUtilization("CS-TUA");
    }

    @Test
    void ensureGetUtilizationForAircraftThrowsExceptionWhenNoFlightsExist() {
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUA"))
                .thenReturn(List.of());

        assertThrows(pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException.class, () -> {
            service.getUtilizationForAircraft("CS-TUA");
        });
    }

    @Test
    void ensureGetUtilizationForAircraftComputesFlightHoursFromDeparturAndArrival() {
        LocalDateTime dep = LocalDateTime.of(2025, 6, 1, 8, 0);
        LocalDateTime arr = dep.plusMinutes(90);
        ScheduledFlight flight = new ScheduledFlight(mockRoute, aircraftA, dep, arr);

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUA"))
                .thenReturn(List.of(flight));

        AircraftUtilizationDTO dto = service.getUtilizationForAircraft("CS-TUA");

        assertEquals(1.5, dto.getTotalFlightHours(), 0.001);
    }

    @Test
    void ensureGetUtilizationForAircraftGroupsFlightsByMonth() {
        LocalDateTime dep1 = LocalDateTime.of(2025, 7, 1, 8, 0);
        LocalDateTime arr1 = dep1.plusHours(2);
        LocalDateTime dep2 = LocalDateTime.of(2025, 7, 15, 8, 0);
        LocalDateTime arr2 = dep2.plusHours(2);
        LocalDateTime dep3 = LocalDateTime.of(2025, 8, 1, 8, 0);
        LocalDateTime arr3 = dep3.plusHours(3);

        ScheduledFlight f1 = new ScheduledFlight(mockRoute, aircraftA, dep1, arr1);
        ScheduledFlight f2 = new ScheduledFlight(mockRoute, aircraftA, dep2, arr2);
        ScheduledFlight f3 = new ScheduledFlight(mockRoute, aircraftA, dep3, arr3);

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUA"))
                .thenReturn(List.of(f1, f2, f3));

        AircraftUtilizationDTO dto = service.getUtilizationForAircraft("CS-TUA");

        assertEquals(2, dto.getUtilizationByPeriod().size());
        assertEquals(7, dto.getUtilizationByPeriod().get(0).getMonth());
        assertEquals(2, dto.getUtilizationByPeriod().get(0).getTotalFlights());
        assertEquals(4.0, dto.getUtilizationByPeriod().get(0).getTotalFlightHours(), 0.001);
        assertEquals(8, dto.getUtilizationByPeriod().get(1).getMonth());
        assertEquals(1, dto.getUtilizationByPeriod().get(1).getTotalFlights());
        assertEquals(3.0, dto.getUtilizationByPeriod().get(1).getTotalFlightHours(), 0.001);
    }

    @Test
    void ensureRepositoryIsCalledWithCorrectRegistrationNumber() {
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUB"))
                .thenReturn(List.of());

        assertThrows(pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException.class, () -> {
            service.getUtilizationForAircraft("CS-TUB");
        });

        verify(scheduledFlightRepository, times(1))
                .findNonCancelledFlightsForUtilization("CS-TUB");
        verify(scheduledFlightRepository, never())
                .findNonCancelledFlightsForUtilization(null);
    }
}