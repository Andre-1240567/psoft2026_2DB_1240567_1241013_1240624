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
import pt.isep.psoft.alsafe.airportmanagement.domain.Airport;
import pt.isep.psoft.alsafe.airportmanagement.services.AirportService;
import pt.isep.psoft.alsafe.flightroutes.api.FlightRouteResponseDTO;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;
import pt.isep.psoft.alsafe.security.SecurityConfig;
import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AirportController.class)
@Import(SecurityConfig.class)
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

    @BeforeEach
    void setUp() {
        Mockito.when(airportModelAssembler.toModel(any(Airport.class)))
               .thenReturn(Mockito.mock(AirportViewDTO.class));
        Mockito.when(airportModelAssembler.toCollectionModel(any()))
               .thenReturn(CollectionModel.of(Collections.emptyList()));
    }

    @Test
    @WithMockUser
    void createAirport_ShouldReturn201_WhenValidRequest() throws Exception {
        CreateAirportRequestDTO request = new CreateAirportRequestDTO();
        request.setIataCode("OPO");
        request.setName("Francisco Sa Carneiro");
        request.setTimezone("UTC+01:00");
        request.setRegion("Europe");
        request.setCity("Porto");
        request.setCountry("Portugal");
        request.setLatitude(41.2356);
        request.setLongitude(-8.6780);
        request.setRunways(Collections.emptyList());

        Mockito.when(airportService.createAirport(any(CreateAirportRequestDTO.class))).thenReturn(Mockito.mock(Airport.class));

        mockMvc.perform(post("/api/airports")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void getRoutesByAirport_ShouldReturn200_WhenUserIsATCC() throws Exception {
        Mockito.when(flightRouteService.getRoutesByAirport(eq("OPO"), any(Pageable.class)))
               .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/airports/OPO/routes"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void getBusiestAirports_ShouldReturn200_WhenUserIsBackofficeOperator() throws Exception {
        Mockito.when(flightRouteService.getBusiestAirports()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/airports/statistics/busiest"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void getAirportsGroupedBy_ShouldReturn200_WhenUserIsATCC() throws Exception {
        Mockito.when(airportService.getAirportsGroupedBy("country")).thenReturn(Collections.emptyMap());

        mockMvc.perform(get("/api/airports/grouped")
                .param("groupBy", "country"))
                .andExpect(status().isOk());
    }
}