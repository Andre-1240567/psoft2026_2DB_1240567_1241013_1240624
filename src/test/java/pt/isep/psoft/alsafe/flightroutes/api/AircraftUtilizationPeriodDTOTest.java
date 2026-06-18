package pt.isep.psoft.alsafe.flightroutes.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AircraftUtilizationDtoTest {

    @Test
    void ensurePeriodDtoStoresYearMonthFlightsAndHours() {
        AircraftUtilizationPeriodDTO period = new AircraftUtilizationPeriodDTO(2025, 3, 10, 45.75);

        assertEquals(2025, period.getYear());
        assertEquals(3, period.getMonth());
        assertEquals(10, period.getTotalFlights());
        assertEquals(45.75, period.getTotalFlightHours());
    }

    @Test
    void ensurePeriodDtoRoundsHoursToTwoDecimalPlaces() {
        AircraftUtilizationPeriodDTO period = new AircraftUtilizationPeriodDTO(2025, 1, 5, 12.3456789);

        assertEquals(12.35, period.getTotalFlightHours());
    }

    @Test
    void ensurePeriodDtoHandlesZeroFlights() {
        AircraftUtilizationPeriodDTO period = new AircraftUtilizationPeriodDTO(2025, 6, 0, 0.0);

        assertEquals(0, period.getTotalFlights());
        assertEquals(0.0, period.getTotalFlightHours());
    }

    @Test
    void ensureUtilizationDtoAggregatesPeriodsCorrectly() {
        AircraftUtilizationPeriodDTO p1 = new AircraftUtilizationPeriodDTO(2025, 1, 4, 20.0);
        AircraftUtilizationPeriodDTO p2 = new AircraftUtilizationPeriodDTO(2025, 2, 6, 30.5);

        AircraftUtilizationDTO dto = new AircraftUtilizationDTO("CS-TUA", "Boeing 737", List.of(p1, p2));

        assertEquals("CS-TUA", dto.getRegistrationNumber());
        assertEquals("Boeing 737", dto.getModelName());
        assertEquals(2, dto.getUtilizationByPeriod().size());
        assertEquals(10, dto.getTotalFlights());
        assertEquals(50.5, dto.getTotalFlightHours(), 0.001);
    }

    @Test
    void ensureUtilizationDtoWithEmptyPeriodsHasZeroTotals() {
        AircraftUtilizationDTO dto = new AircraftUtilizationDTO("CS-TUA", "Boeing 737", List.of());

        assertEquals(0, dto.getTotalFlights());
        assertEquals(0.0, dto.getTotalFlightHours(), 0.001);
        assertTrue(dto.getUtilizationByPeriod().isEmpty());
    }

    @Test
    void ensureUtilizationDtoAcceptsNullModelName() {
        AircraftUtilizationDTO dto = new AircraftUtilizationDTO("CS-TUA", null, List.of());

        assertEquals("CS-TUA", dto.getRegistrationNumber());
        assertNull(dto.getModelName());
    }

    @Test
    void ensureUtilizationDtoTotalFlightHoursSumsAllPeriods() {
        AircraftUtilizationPeriodDTO p1 = new AircraftUtilizationPeriodDTO(2025, 1, 2, 10.0);
        AircraftUtilizationPeriodDTO p2 = new AircraftUtilizationPeriodDTO(2025, 2, 3, 15.0);
        AircraftUtilizationPeriodDTO p3 = new AircraftUtilizationPeriodDTO(2025, 3, 1, 5.5);

        AircraftUtilizationDTO dto = new AircraftUtilizationDTO("CS-ABC", "A320", List.of(p1, p2, p3));

        assertEquals(30.5, dto.getTotalFlightHours(), 0.001);
        assertEquals(6, dto.getTotalFlights());
    }

    @Test
    void ensureUtilizationDtoTotalFlightsSumsAllPeriods() {
        AircraftUtilizationPeriodDTO p1 = new AircraftUtilizationPeriodDTO(2025, 4, 7, 35.0);
        AircraftUtilizationPeriodDTO p2 = new AircraftUtilizationPeriodDTO(2025, 5, 3, 12.0);

        AircraftUtilizationDTO dto = new AircraftUtilizationDTO("CS-XYZ", "A321", List.of(p1, p2));

        assertEquals(10, dto.getTotalFlights());
    }
}