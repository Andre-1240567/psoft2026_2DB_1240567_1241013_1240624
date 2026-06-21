package pt.isep.psoft.alsafe.airportmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pt.isep.psoft.alsafe.airportmanagement.api.dto.*;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;
import pt.isep.psoft.alsafe.security.SecurityConfig;
import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;
import pt.isep.psoft.alsafe.shared.api.GlobalExceptionHandler;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class AirportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AirportService airportService;

    @MockBean
    private FlightRouteService flightRouteService;

    @MockBean
    private AirportModelAssembler airportModelAssembler;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    private CreateAirportRequestDTO validCreateRequest;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            
            chain.doFilter(request, response);
            return null;
        }).when(authTokenFilter).doFilter(any(), any(), any());

        Mockito.when(airportModelAssembler.toModel(any(Airport.class)))
               .thenReturn(Mockito.mock(AirportViewDTO.class));
        Mockito.when(airportModelAssembler.toCollectionModel(any()))
               .thenReturn(CollectionModel.of(Collections.emptyList()));

        validCreateRequest = new CreateAirportRequestDTO();
        validCreateRequest.setIataCode("OPO");
        validCreateRequest.setName("Francisco Sa Carneiro");
        validCreateRequest.setTimezone("UTC+01:00");
        validCreateRequest.setRegion("Europe");
        validCreateRequest.setCity("Porto");
        validCreateRequest.setCountry("Portugal");
        validCreateRequest.setLatitude(41.2356);
        validCreateRequest.setLongitude(-8.6780);
        validCreateRequest.setRunways(Collections.emptyList());
    }


    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void createAirport_ShouldReturn201_WhenBackofficeOperatorAndValidRequest() throws Exception {
        Mockito.when(airportService.createAirport(any())).thenReturn(Mockito.mock(Airport.class));

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void createAirport_ShouldReturn403_WhenRoleIsATCC() throws Exception {
        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAirport_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void createAirport_ShouldReturn400_WhenIataCodeHasInvalidFormat() throws Exception {
        validCreateRequest.setIataCode("op");

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void createAirport_ShouldReturn400_WhenNameIsBlank() throws Exception {
        validCreateRequest.setName("");

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void createAirport_ShouldReturn400_WhenTimezoneHasInvalidFormat() throws Exception {
        validCreateRequest.setTimezone("GMT+1");

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void createAirport_ShouldReturn400_WhenRunwaysIsNull() throws Exception {
        validCreateRequest.setRunways(null);

        mockMvc.perform(post("/api/airports")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void getAirportDetails_ShouldReturn200_WhenBackofficeOperator() throws Exception {
        Mockito.when(airportService.getAirportDetails("OPO")).thenReturn(Mockito.mock(Airport.class));

        mockMvc.perform(get("/api/airports/OPO"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void getAirportDetails_ShouldReturn200_WhenATCC() throws Exception {
        Mockito.when(airportService.getAirportDetails("OPO")).thenReturn(Mockito.mock(Airport.class));

        mockMvc.perform(get("/api/airports/OPO"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MAINTENANCE_TECHNICIAN")
    void getAirportDetails_ShouldReturn403_WhenRoleHasNoAccess() throws Exception {
        mockMvc.perform(get("/api/airports/OPO"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAirportDetails_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/airports/OPO"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(roles = "ATCC")
    void searchAirports_ShouldReturn200_WhenCityProvided() throws Exception {
        Mockito.when(airportService.searchAirports("Porto", null, null))
               .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/airports").param("city", "Porto"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void searchAirports_ShouldReturn200_WhenCountryProvided() throws Exception {
        Mockito.when(airportService.searchAirports(null, "Portugal", null))
               .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/airports").param("country", "Portugal"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void searchAirports_ShouldReturn200_WhenNameProvided() throws Exception {
        Mockito.when(airportService.searchAirports(null, null, "Sá Carneiro"))
               .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/airports").param("name", "Sá Carneiro"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void searchAirports_ShouldReturn400_WhenNoParamProvided() throws Exception {
        mockMvc.perform(get("/api/airports"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void searchAirports_ShouldReturn403_WhenRoleIsBackofficeOperator() throws Exception {
        mockMvc.perform(get("/api/airports").param("city", "Porto"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchAirports_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/airports").param("city", "Porto"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void changeOperationalStatus_ShouldReturn200_WhenBackofficeOperator() throws Exception {
        ChangeAirportStatusDTO dto = new ChangeAirportStatusDTO();
        dto.setNewStatus("CLOSED");

        Mockito.when(airportService.changeOperationalStatus("OPO", "CLOSED"))
               .thenReturn(Mockito.mock(Airport.class));

        mockMvc.perform(patch("/api/airports/OPO/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void changeOperationalStatus_ShouldReturn403_WhenRoleIsATCC() throws Exception {
        ChangeAirportStatusDTO dto = new ChangeAirportStatusDTO();
        dto.setNewStatus("CLOSED");

        mockMvc.perform(patch("/api/airports/OPO/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void changeOperationalStatus_ShouldReturn400_WhenStatusIsBlank() throws Exception {
        ChangeAirportStatusDTO dto = new ChangeAirportStatusDTO();
        dto.setNewStatus("");

        mockMvc.perform(patch("/api/airports/OPO/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void addCertification_ShouldReturn200_WhenBackofficeOperator() throws Exception {
        AddCertificationDTO dto = new AddCertificationDTO();
        dto.setAircraftModelName("737 MAX");

        Mockito.when(airportService.addAirplaneCertification("OPO", "737 MAX"))
               .thenReturn(Mockito.mock(Airport.class));

        mockMvc.perform(post("/api/airports/OPO/certifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void addCertification_ShouldReturn200_WhenATCC() throws Exception {
        AddCertificationDTO dto = new AddCertificationDTO();
        dto.setAircraftModelName("A320neo");

        Mockito.when(airportService.addAirplaneCertification("LIS", "A320neo"))
               .thenReturn(Mockito.mock(Airport.class));

        mockMvc.perform(post("/api/airports/LIS/certifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MAINTENANCE_TECHNICIAN")
    void addCertification_ShouldReturn403_WhenRoleHasNoAccess() throws Exception {
        AddCertificationDTO dto = new AddCertificationDTO();
        dto.setAircraftModelName("737 MAX");

        mockMvc.perform(post("/api/airports/OPO/certifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void addCertification_ShouldReturn400_WhenModelNameIsBlank() throws Exception {
        AddCertificationDTO dto = new AddCertificationDTO();
        dto.setAircraftModelName("");

        mockMvc.perform(post("/api/airports/OPO/certifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void updateAirportDetails_ShouldReturn200_WhenBackofficeOperator() throws Exception {
        UpdateAirportDetailsRequestDTO dto = new UpdateAirportDetailsRequestDTO();
        OperationalHoursDTO opHours = new OperationalHoursDTO();
        opHours.setOpeningTime("08:00");
        opHours.setClosingTime("22:00");
        dto.setOperationalHours(opHours);

        Mockito.when(airportService.updateAirportDetails(eq("OPO"), any()))
               .thenReturn(Mockito.mock(Airport.class));

        mockMvc.perform(patch("/api/airports/OPO/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void updateAirportDetails_ShouldReturn403_WhenRoleIsATCC() throws Exception {
        UpdateAirportDetailsRequestDTO dto = new UpdateAirportDetailsRequestDTO();

        mockMvc.perform(patch("/api/airports/OPO/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void updateAirportDetails_ShouldReturn400_WhenOperationalHoursFormatIsInvalid() throws Exception {
        UpdateAirportDetailsRequestDTO dto = new UpdateAirportDetailsRequestDTO();
        OperationalHoursDTO opHours = new OperationalHoursDTO();
        opHours.setOpeningTime("8am");
        opHours.setClosingTime("22:00");
        dto.setOperationalHours(opHours);

        mockMvc.perform(patch("/api/airports/OPO/details")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(roles = "ATCC")
    void getRoutesByAirport_ShouldReturn200_WhenATCC() throws Exception {
        Mockito.when(flightRouteService.getRoutesByAirport(eq("OPO"), any(Pageable.class)))
               .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/airports/OPO/routes"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void getRoutesByAirport_ShouldReturn403_WhenRoleIsBackofficeOperator() throws Exception {
        mockMvc.perform(get("/api/airports/OPO/routes"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRoutesByAirport_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/airports/OPO/routes"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void getBusiestAirports_ShouldReturn200_WhenBackofficeOperator() throws Exception {
        BusiestAirportDTO fakeBusiest = new BusiestAirportDTO("OPO", 150L);
        Mockito.when(flightRouteService.getBusiestAirports()).thenReturn(List.of(fakeBusiest));

        mockMvc.perform(get("/api/airports/statistics/busiest"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void getBusiestAirports_ShouldReturn403_WhenRoleIsATCC() throws Exception {
        mockMvc.perform(get("/api/airports/statistics/busiest"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getBusiestAirports_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/airports/statistics/busiest"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void getAirportsGroupedBy_ShouldReturn200_WhenATCC() throws Exception {
        Airport mockAirport = Mockito.mock(Airport.class);
        
        Map<String, List<Airport>> fakeMap = Map.of("Portugal", List.of(mockAirport));
        Mockito.when(airportService.getAirportsGroupedBy("country")).thenReturn(fakeMap);

        mockMvc.perform(get("/api/airports/grouped").param("groupBy", "country"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void getAirportsGroupedBy_ShouldReturn200_WhenGroupedByRegion() throws Exception {
        Airport mockAirport = Mockito.mock(Airport.class);
        Map<String, List<Airport>> fakeMap = Map.of("Europe", List.of(mockAirport));
        Mockito.when(airportService.getAirportsGroupedBy("region")).thenReturn(fakeMap);

        mockMvc.perform(get("/api/airports/grouped").param("groupBy", "region"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void getAirportsGroupedBy_ShouldReturn403_WhenRoleIsBackofficeOperator() throws Exception {
        mockMvc.perform(get("/api/airports/grouped").param("groupBy", "country"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAirportsGroupedBy_ShouldReturn401_WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/airports/grouped").param("groupBy", "country"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void getAirportsGroupedBy_ShouldReturn400_WhenInvalidCriteria() throws Exception {
        Mockito.when(airportService.getAirportsGroupedBy("invalid"))
               .thenThrow(new IllegalArgumentException("Invalid grouping criteria."));

        mockMvc.perform(get("/api/airports/grouped").param("groupBy", "invalid"))
                .andExpect(status().isBadRequest());
    }
}