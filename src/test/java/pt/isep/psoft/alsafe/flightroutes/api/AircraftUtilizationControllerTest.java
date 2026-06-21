package pt.isep.psoft.alsafe.flightroutes.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import pt.isep.psoft.alsafe.flightroutes.api.dto.AircraftUtilizationDTO;
import pt.isep.psoft.alsafe.flightroutes.api.dto.AircraftUtilizationPeriodDTO;
import pt.isep.psoft.alsafe.flightroutes.services.AircraftUtilizationService;
import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AircraftUtilizationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AircraftUtilizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AircraftUtilizationService utilizationService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    private AircraftUtilizationDTO dtoA;
    private AircraftUtilizationDTO dtoB;

    @BeforeEach
    void setUp() {
        AircraftUtilizationPeriodDTO period1 = new AircraftUtilizationPeriodDTO(2025, 1, 4, 20.0);
        AircraftUtilizationPeriodDTO period2 = new AircraftUtilizationPeriodDTO(2025, 2, 6, 30.0);

        dtoA = new AircraftUtilizationDTO("CS-TUA", "Boeing 737", List.of(period1, period2));
        dtoB = new AircraftUtilizationDTO("CS-TUB", "Airbus A320", List.of(period1));
    }





    @Test
    void ensureGetAllAircraftUtilizationReturns200OK() throws Exception {
        when(utilizationService.getUtilizationForAllAircraft())
                .thenReturn(List.of(dtoA, dtoB));

        mockMvc.perform(get("/api/aircraft-utilization"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetAllAircraftUtilizationReturnsListWithCorrectSize() throws Exception {
        when(utilizationService.getUtilizationForAllAircraft())
                .thenReturn(List.of(dtoA, dtoB));

        mockMvc.perform(get("/api/aircraft-utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.utilizations.length()").value(2));
    }

    @Test
    void ensureGetAllAircraftUtilizationReturnsCorrectRegistrationNumbers() throws Exception {
        when(utilizationService.getUtilizationForAllAircraft())
                .thenReturn(List.of(dtoA, dtoB));

        mockMvc.perform(get("/api/aircraft-utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.utilizations[0].registrationNumber").value("CS-TUA"))
                .andExpect(jsonPath("$._embedded.utilizations[1].registrationNumber").value("CS-TUB"));
    }

    @Test
    void ensureGetAllAircraftUtilizationReturnsModelNames() throws Exception {
        when(utilizationService.getUtilizationForAllAircraft())
                .thenReturn(List.of(dtoA));

        mockMvc.perform(get("/api/aircraft-utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.utilizations[0].modelName").value("Boeing 737"));
    }

    @Test
    void ensureGetAllAircraftUtilizationReturnsTotals() throws Exception {
        when(utilizationService.getUtilizationForAllAircraft())
                .thenReturn(List.of(dtoA));

        mockMvc.perform(get("/api/aircraft-utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.utilizations[0].totalFlights").value(10))
                .andExpect(jsonPath("$._embedded.utilizations[0].totalFlightHours").value(50.0));
    }

    @Test
    void ensureGetAllAircraftUtilizationReturnsPeriodBreakdown() throws Exception {
        when(utilizationService.getUtilizationForAllAircraft())
                .thenReturn(List.of(dtoA));

        mockMvc.perform(get("/api/aircraft-utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.utilizations[0].utilizationByPeriod").isArray())
                .andExpect(jsonPath("$._embedded.utilizations[0].utilizationByPeriod.length()").value(2))
                .andExpect(jsonPath("$._embedded.utilizations[0].utilizationByPeriod[0].year").value(2025))
                .andExpect(jsonPath("$._embedded.utilizations[0].utilizationByPeriod[0].month").value(1))
                .andExpect(jsonPath("$._embedded.utilizations[0].utilizationByPeriod[0].totalFlights").value(4))
                .andExpect(jsonPath("$._embedded.utilizations[0].utilizationByPeriod[0].totalFlightHours").value(20.0));
    }

    @Test
    void ensureGetAllAircraftUtilizationContainsSelfLinks() throws Exception {
        when(utilizationService.getUtilizationForAllAircraft())
                .thenReturn(List.of(dtoA));

        mockMvc.perform(get("/api/aircraft-utilization"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.utilizations[0]._links.self").exists())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void ensureGetAllAircraftUtilizationReturnsEmptyListWhenNoFlights() throws Exception {
        when(utilizationService.getUtilizationForAllAircraft())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/aircraft-utilization"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$._embedded").doesNotExist());
    }





    @Test
    void ensureGetAircraftUtilizationReturns200OK() throws Exception {
        when(utilizationService.getUtilizationForAircraft("CS-TUA"))
                .thenReturn(dtoA);

        mockMvc.perform(get("/api/aircraft-utilization/CS-TUA"))
                .andExpect(status().isOk());
    }

    @Test
    void ensureGetAircraftUtilizationReturnsCorrectRegistration() throws Exception {
        when(utilizationService.getUtilizationForAircraft("CS-TUA"))
                .thenReturn(dtoA);

        mockMvc.perform(get("/api/aircraft-utilization/CS-TUA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TUA"));
    }

    @Test
    void ensureGetAircraftUtilizationReturnsModelName() throws Exception {
        when(utilizationService.getUtilizationForAircraft("CS-TUA"))
                .thenReturn(dtoA);

        mockMvc.perform(get("/api/aircraft-utilization/CS-TUA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName").value("Boeing 737"));
    }

    @Test
    void ensureGetAircraftUtilizationReturnsTotalFlightsAndHours() throws Exception {
        when(utilizationService.getUtilizationForAircraft("CS-TUA"))
                .thenReturn(dtoA);

        mockMvc.perform(get("/api/aircraft-utilization/CS-TUA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFlights").value(10))
                .andExpect(jsonPath("$.totalFlightHours").value(50.0));
    }

    @Test
    void ensureGetAircraftUtilizationReturnsPeriodBreakdown() throws Exception {
        when(utilizationService.getUtilizationForAircraft("CS-TUA"))
                .thenReturn(dtoA);

        mockMvc.perform(get("/api/aircraft-utilization/CS-TUA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.utilizationByPeriod").isArray())
                .andExpect(jsonPath("$.utilizationByPeriod.length()").value(2))
                .andExpect(jsonPath("$.utilizationByPeriod[1].month").value(2))
                .andExpect(jsonPath("$.utilizationByPeriod[1].totalFlights").value(6))
                .andExpect(jsonPath("$.utilizationByPeriod[1].totalFlightHours").value(30.0));
    }

    @Test
    void ensureGetAircraftUtilizationContainsSelfLink() throws Exception {
        when(utilizationService.getUtilizationForAircraft("CS-TUA"))
                .thenReturn(dtoA);

        mockMvc.perform(get("/api/aircraft-utilization/CS-TUA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void ensureGetAircraftUtilizationContainsAllAircraftLink() throws Exception {
        when(utilizationService.getUtilizationForAircraft("CS-TUA"))
                .thenReturn(dtoA);

        mockMvc.perform(get("/api/aircraft-utilization/CS-TUA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.all-aircraft-utilization").exists());
    }

    @Test
    void ensureGetAircraftUtilizationReturnsEmptyPeriodsWhenAircraftHasNoFlights() throws Exception {
        AircraftUtilizationDTO emptyDto = new AircraftUtilizationDTO("CS-NEW", null, List.of());

        when(utilizationService.getUtilizationForAircraft("CS-NEW"))
                .thenReturn(emptyDto);

        mockMvc.perform(get("/api/aircraft-utilization/CS-NEW"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("CS-NEW"))
                .andExpect(jsonPath("$.totalFlights").value(0))
                .andExpect(jsonPath("$.totalFlightHours").value(0.0))
                .andExpect(jsonPath("$.utilizationByPeriod").isEmpty());
    }
}