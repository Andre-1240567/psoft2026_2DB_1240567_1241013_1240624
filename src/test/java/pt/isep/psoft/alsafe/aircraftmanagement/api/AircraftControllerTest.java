package pt.isep.psoft.alsafe.aircraftmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.services.AircraftService;
import pt.isep.psoft.alsafe.security.jwt.JwtUtils;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AircraftController.class)
@AutoConfigureMockMvc(addFilters = false)
class AircraftControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AircraftService aircraftService;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private FlightRouteService flightRouteService;

    @Autowired
    private ObjectMapper objectMapper;

    private Aircraft mockAircraft;

    @BeforeEach
    void setUp() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "A320neo", 160, 24000.0, 6300.0, 828.0);
        mockAircraft = new Aircraft("CS-TPA", model, LocalDate.now(), "Economy");
    }

    @Test
    void ensureCreateAircraftReturns201Created() throws Exception {
        CreateAircraftDTO dto = new CreateAircraftDTO();
        dto.setRegistrationNumber("CS-TPA");
        dto.setModelName("A320neo");
        dto.setManufacturingDate(LocalDate.now());
        dto.setActiveConfigurationName("Economy");

        when(aircraftService.createAircraft(any(CreateAircraftDTO.class))).thenReturn(mockAircraft);

        mockMvc.perform(post("/api/aircrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("CS-TPA"));
    }

    @Test
    void ensureGetAircraftDetailsReturns200OK() throws Exception {
        when(aircraftService.getAircraftDetails("CS-TPA")).thenReturn(mockAircraft);

        mockMvc.perform(get("/api/aircrafts/CS-TPA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelName").value("A320neo"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ATCC")
    void ensureGetCompatibleRoutesReturns200OK() throws Exception {
        when(aircraftService.getAircraftDetails("CS-TPA")).thenReturn(mockAircraft);
        
        pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO routeDto = 
            org.mockito.Mockito.mock(pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO.class);
        when(routeDto.getOriginIataCode()).thenReturn("LIS");
        when(routeDto.getDestinationIataCode()).thenReturn("OPO");

        when(flightRouteService.getCompatibleRoutesForAircraft(any(Double.class), any(Integer.class)))
            .thenReturn(java.util.List.of(routeDto));

        mockMvc.perform(get("/api/aircrafts/CS-TPA/compatible-routes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].originIataCode").value("LIS"))
                .andExpect(jsonPath("$[0].destinationIataCode").value("OPO"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ATCC")
    void ensureGetStatusOverviewReturns200OK() throws Exception {
        AircraftStatusOverviewDTO overview = new AircraftStatusOverviewDTO();
        overview.addAircraftToStatus("AVAILABLE", new AircraftResponseDTO(mockAircraft));

        when(aircraftService.getAircraftStatusOverview()).thenReturn(overview);

        mockMvc.perform(get("/api/aircrafts/status-overview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAvailable").value(1))
                .andExpect(jsonPath("$.aircraftsByStatus.AVAILABLE[0].registrationNumber").value("CS-TPA"));
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ATCC")
    void ensureGetOperationalHoursReturns200OK() throws Exception {
        AircraftOperationalHoursDTO dto = new AircraftOperationalHoursDTO(mockAircraft);

        when(aircraftService.getAircraftsOperationalHours()).thenReturn(java.util.List.of(dto));

        mockMvc.perform(get("/api/aircrafts/operational-hours")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber").value("CS-TPA"));
    }
}