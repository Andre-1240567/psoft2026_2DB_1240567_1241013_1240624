package pt.isep.psoft.alsafe.flightroutes.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.domain.IATACode;
import pt.isep.psoft.alsafe.flightroutes.api.AircraftFuelEfficiencyDTO;
import pt.isep.psoft.alsafe.flightroutes.api.RouteFuelEfficiencyDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;
import pt.isep.psoft.alsafe.flightroutes.repositories.FlightRouteRepository;
import pt.isep.psoft.alsafe.flightroutes.repositories.ScheduledFlightRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FuelEfficiencyService (US227).
 * Covers every branch in the service: happy paths, empty-flight paths,
 * not-found paths, multi-aircraft/multi-route aggregation, and zero-burnRate guard.
 */
@ExtendWith(MockitoExtension.class)
class FuelEfficiencyServiceTest {

    @Mock private ScheduledFlightRepository scheduledFlightRepository;
    @Mock private FlightRouteRepository     flightRouteRepository;
    @Mock private AircraftRepository        aircraftRepository;

    @InjectMocks
    private FuelEfficiencyService service;





    private AircraftModel modelA;
    private AircraftModel modelB;
    private Aircraft      aircraftA;
    private Aircraft      aircraftB;
    private FlightRoute   routeX;
    private FlightRoute   routeY;
    private ScheduledFlight flightA1;
    private ScheduledFlight flightA2;
    private ScheduledFlight flightB1;

    @BeforeEach
    void setUp() {

        modelA = new AircraftModel(Manufacturer.BOEING, "737-800", 189, 20000.0, 5000.0, 842.0);
        modelB = new AircraftModel(Manufacturer.AIRBUS, "A320", 150, 10000.0, 2000.0, 833.0);


        aircraftA = new Aircraft("CS-TUA", modelA, LocalDate.of(2015, 1, 1), "Standard");
        aircraftB = new Aircraft("CS-TUB", modelB, LocalDate.of(2018, 6, 1), "Standard");


        Airport origin = mock(Airport.class);
        Airport dest   = mock(Airport.class);
        IATACode lisCode = mock(IATACode.class);
        IATACode opoCode = mock(IATACode.class);
        when(origin.getIataCode()).thenReturn(lisCode);
        when(lisCode.getCode()).thenReturn("LIS");
        when(dest.getIataCode()).thenReturn(opoCode);
        when(opoCode.getCode()).thenReturn("OPO");

        Airport originY = mock(Airport.class);
        Airport destY   = mock(Airport.class);
        IATACode faoCode = mock(IATACode.class);
        IATACode madCode = mock(IATACode.class);
        when(originY.getIataCode()).thenReturn(faoCode);
        when(faoCode.getCode()).thenReturn("FAO");
        when(destY.getIataCode()).thenReturn(madCode);
        when(madCode.getCode()).thenReturn("MAD");


        routeX = new FlightRoute("ROUTE-X", origin, dest,
                1000.0, 90, new RouteRequirement(5500.0, 100), "system");
        routeY = new FlightRoute("ROUTE-Y", originY, destY,
                500.0, 45, new RouteRequirement(2500.0, 80), "system");


        LocalDateTime dep1 = LocalDateTime.of(2025, 3, 10, 8, 0);
        LocalDateTime arr1 = LocalDateTime.of(2025, 3, 10, 9, 30);
        flightA1 = new ScheduledFlight(routeX, aircraftA, dep1, arr1);

        LocalDateTime dep2 = LocalDateTime.of(2025, 4, 1, 10, 0);
        LocalDateTime arr2 = LocalDateTime.of(2025, 4, 1, 11, 30);
        flightA2 = new ScheduledFlight(routeX, aircraftA, dep2, arr2);

        LocalDateTime dep3 = LocalDateTime.of(2025, 5, 5, 7, 0);
        LocalDateTime arr3 = LocalDateTime.of(2025, 5, 5, 7, 45);
        flightB1 = new ScheduledFlight(routeY, aircraftB, dep3, arr3);
    }





    @Test
    void getEfficiencyForAllAircraft_withFlights_returnsCorrectMetrics() {
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of(flightA1, flightA2, flightB1));

        List<AircraftFuelEfficiencyDTO> result = service.getEfficiencyForAllAircraft();

        assertThat(result).hasSize(2);

        AircraftFuelEfficiencyDTO dtoA = result.stream()
                .filter(d -> d.getRegistrationNumber().equals("CS-TUA"))
                .findFirst().orElseThrow();


        assertThat(dtoA.getFuelBurnRateLPerKm()).isEqualTo(4.0);

        assertThat(dtoA.getTotalDistanceFlownKm()).isEqualTo(2000.0);

        assertThat(dtoA.getTotalEstimatedFuelL()).isEqualTo(8000.0);

        assertThat(dtoA.getEfficiencyKmPerL()).isEqualTo(0.25);
        assertThat(dtoA.getFlightCount()).isEqualTo(2);

        AircraftFuelEfficiencyDTO dtoB = result.stream()
                .filter(d -> d.getRegistrationNumber().equals("CS-TUB"))
                .findFirst().orElseThrow();


        assertThat(dtoB.getFuelBurnRateLPerKm()).isEqualTo(5.0);
        assertThat(dtoB.getTotalDistanceFlownKm()).isEqualTo(500.0);
        assertThat(dtoB.getTotalEstimatedFuelL()).isEqualTo(2500.0);
        assertThat(dtoB.getEfficiencyKmPerL()).isEqualTo(0.2);
        assertThat(dtoB.getFlightCount()).isEqualTo(1);
    }

    @Test
    void getEfficiencyForAllAircraft_noFlights_returnsEmptyList() {
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of());

        List<AircraftFuelEfficiencyDTO> result = service.getEfficiencyForAllAircraft();

        assertThat(result).isEmpty();
    }





    @Test
    void getEfficiencyForAircraft_withFlights_returnsCorrectMetrics() {
        when(aircraftRepository.existsById("CS-TUA")).thenReturn(true);
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUA"))
                .thenReturn(List.of(flightA1, flightA2));

        AircraftFuelEfficiencyDTO dto = service.getEfficiencyForAircraft("CS-TUA");

        assertThat(dto.getRegistrationNumber()).isEqualTo("CS-TUA");
        assertThat(dto.getFuelBurnRateLPerKm()).isEqualTo(4.0);
        assertThat(dto.getTotalDistanceFlownKm()).isEqualTo(2000.0);
        assertThat(dto.getFlightCount()).isEqualTo(2);
    }

    @Test
    void getEfficiencyForAircraft_registrationIsUppercased() {
        when(aircraftRepository.existsById("CS-TUA")).thenReturn(true);
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUA"))
                .thenReturn(List.of(flightA1));


        AircraftFuelEfficiencyDTO dto = service.getEfficiencyForAircraft("cs-tua");

        assertThat(dto.getRegistrationNumber()).isEqualTo("CS-TUA");
        verify(aircraftRepository).existsById("CS-TUA");
    }





    @Test
    void getEfficiencyForAircraft_aircraftNotFound_throwsResourceNotFoundException() {
        when(aircraftRepository.existsById("UNKNOWN")).thenReturn(false);

        assertThatThrownBy(() -> service.getEfficiencyForAircraft("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }





    @Test
    void getEfficiencyForAircraft_noFlights_returnsZeroMetrics() {
        when(aircraftRepository.existsById("CS-TUA")).thenReturn(true);
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUA"))
                .thenReturn(List.of());
        when(aircraftRepository.findById("CS-TUA")).thenReturn(Optional.of(aircraftA));

        AircraftFuelEfficiencyDTO dto = service.getEfficiencyForAircraft("CS-TUA");

        assertThat(dto.getRegistrationNumber()).isEqualTo("CS-TUA");
        assertThat(dto.getTotalDistanceFlownKm()).isEqualTo(0.0);
        assertThat(dto.getTotalEstimatedFuelL()).isEqualTo(0.0);
        assertThat(dto.getFlightCount()).isEqualTo(0);

        assertThat(dto.getEfficiencyKmPerL()).isEqualTo(0.25);
    }

    @Test
    void getEfficiencyForAircraft_noFlights_andFindByIdEmpty_throwsResourceNotFoundException() {

        when(aircraftRepository.existsById("CS-TUA")).thenReturn(true);
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-TUA"))
                .thenReturn(List.of());
        when(aircraftRepository.findById("CS-TUA")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEfficiencyForAircraft("CS-TUA"))
                .isInstanceOf(ResourceNotFoundException.class);
    }





    @Test
    void getEfficiencyForAllRoutes_withFlights_returnsCorrectMetrics() {
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of(flightA1, flightB1));

        List<RouteFuelEfficiencyDTO> result = service.getEfficiencyForAllRoutes();

        assertThat(result).hasSize(2);

        RouteFuelEfficiencyDTO dtoX = result.stream()
                .filter(d -> d.getRouteId().equals("ROUTE-X"))
                .findFirst().orElseThrow();

        assertThat(dtoX.getOriginIata()).isEqualTo("LIS");
        assertThat(dtoX.getDestinationIata()).isEqualTo("OPO");
        assertThat(dtoX.getDistanceKm()).isEqualTo(1000.0);

        assertThat(dtoX.getEstimatedFuelPerFlightL()).isEqualTo(4000.0);
        assertThat(dtoX.getEfficiencyKmPerL()).isEqualTo(0.25);
        assertThat(dtoX.getFlightCount()).isEqualTo(1);
    }

    @Test
    void getEfficiencyForAllRoutes_multipleModelsOnSameRoute_averagesBurnRate() {


        ScheduledFlight flightBonX = new ScheduledFlight(routeX, aircraftB,
                LocalDateTime.of(2025, 6, 1, 8, 0),
                LocalDateTime.of(2025, 6, 1, 9, 30));

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of(flightA1, flightBonX));

        List<RouteFuelEfficiencyDTO> result = service.getEfficiencyForAllRoutes();

        assertThat(result).hasSize(1);
        RouteFuelEfficiencyDTO dto = result.get(0);



        assertThat(dto.getEstimatedFuelPerFlightL()).isEqualTo(4500.0);

        assertThat(dto.getEfficiencyKmPerL()).isCloseTo(1.0 / 4.5, within(0.0001));
        assertThat(dto.getFlightCount()).isEqualTo(2);
    }

    @Test
    void getEfficiencyForAllRoutes_noFlights_returnsEmptyList() {
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of());

        List<RouteFuelEfficiencyDTO> result = service.getEfficiencyForAllRoutes();

        assertThat(result).isEmpty();
    }





    @Test
    void getEfficiencyForRoute_withFlights_returnsCorrectMetrics() {
        when(flightRouteRepository.findById("ROUTE-X")).thenReturn(Optional.of(routeX));
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of(flightA1, flightA2, flightB1));

        RouteFuelEfficiencyDTO dto = service.getEfficiencyForRoute("ROUTE-X");

        assertThat(dto.getRouteId()).isEqualTo("ROUTE-X");

        assertThat(dto.getFlightCount()).isEqualTo(2);
        assertThat(dto.getDistanceKm()).isEqualTo(1000.0);
    }





    @Test
    void getEfficiencyForRoute_routeNotFound_throwsResourceNotFoundException() {
        when(flightRouteRepository.findById("NONEXISTENT")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getEfficiencyForRoute("NONEXISTENT"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("NONEXISTENT");
    }





    @Test
    void getEfficiencyForRoute_noFlights_returnsZeroMetrics() {
        when(flightRouteRepository.findById("ROUTE-X")).thenReturn(Optional.of(routeX));
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of());

        RouteFuelEfficiencyDTO dto = service.getEfficiencyForRoute("ROUTE-X");

        assertThat(dto.getRouteId()).isEqualTo("ROUTE-X");
        assertThat(dto.getDistanceKm()).isEqualTo(1000.0);
        assertThat(dto.getFlightCount()).isEqualTo(0);
        assertThat(dto.getEstimatedFuelPerFlightL()).isEqualTo(0.0);
        assertThat(dto.getEfficiencyKmPerL()).isEqualTo(0.0);
    }





    @Test
    void getEfficiencyForAircraft_modelWithZeroBurnRate_doesNotReturnInfinity() {



        AircraftModel zeroBurnModel = mock(AircraftModel.class);
        when(zeroBurnModel.getModelName()).thenReturn("ZeroModel");
        when(zeroBurnModel.getFuelCapacity()).thenReturn(0.0);
        when(zeroBurnModel.getMaxRange()).thenReturn(1000.0);

        Aircraft zeroBurnAircraft = mock(Aircraft.class);
        when(zeroBurnAircraft.getRegistrationNumber()).thenReturn("CS-ZERO");
        when(zeroBurnAircraft.getModel()).thenReturn(zeroBurnModel);

        when(aircraftRepository.existsById("CS-ZERO")).thenReturn(true);
        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization("CS-ZERO"))
                .thenReturn(List.of());
        when(aircraftRepository.findById("CS-ZERO")).thenReturn(Optional.of(zeroBurnAircraft));

        AircraftFuelEfficiencyDTO dto = service.getEfficiencyForAircraft("CS-ZERO");

        assertThat(dto.getEfficiencyKmPerL()).isEqualTo(0.0);
        assertThat(Double.isInfinite(dto.getEfficiencyKmPerL())).isFalse();
        assertThat(Double.isNaN(dto.getEfficiencyKmPerL())).isFalse();
    }





    @Test
    void accumulators_withZeroBurnRateFlights_handleDivisionByZeroGracefully() {

        AircraftModel zeroBurnModel = mock(AircraftModel.class);
        when(zeroBurnModel.getModelName()).thenReturn("Zero-E");
        when(zeroBurnModel.getFuelCapacity()).thenReturn(0.0);
        when(zeroBurnModel.getMaxRange()).thenReturn(1000.0);

        Aircraft zeroAircraft = mock(Aircraft.class);
        when(zeroAircraft.getRegistrationNumber()).thenReturn("CS-ZERO");
        when(zeroAircraft.getModel()).thenReturn(zeroBurnModel);


        ScheduledFlight zeroFlight = mock(ScheduledFlight.class);
        when(zeroFlight.getAircraft()).thenReturn(zeroAircraft);
        when(zeroFlight.getRoute()).thenReturn(routeX);

        when(scheduledFlightRepository.findNonCancelledFlightsForUtilization(null))
                .thenReturn(List.of(zeroFlight));


        List<AircraftFuelEfficiencyDTO> aircraftResult = service.getEfficiencyForAllAircraft();
        assertThat(aircraftResult).hasSize(1);
        assertThat(aircraftResult.get(0).getEfficiencyKmPerL()).isEqualTo(0.0);


        List<RouteFuelEfficiencyDTO> routeResult = service.getEfficiencyForAllRoutes();
        assertThat(routeResult).hasSize(1);
        assertThat(routeResult.get(0).getEfficiencyKmPerL()).isEqualTo(0.0);
    }
}