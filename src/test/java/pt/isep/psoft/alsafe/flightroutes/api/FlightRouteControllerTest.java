package pt.isep.psoft.alsafe.flightroutes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.services.FlightRouteService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightRouteController.class)
@AutoConfigureMockMvc(addFilters = false)
class FlightRouteControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    @Autowired
    private ObjectMapper objectMapper; 

    // RESTAURADO PARA A ANOTAÇÃO MODERNA
    @MockBean
    private FlightRouteService flightRouteService; 

    private Airport originAirport;
    private Airport destAirport;
    private FlightRoute validRoute;

    @BeforeEach
    void setUp() {
        originAirport = new Airport(new IATACode("OPO"), "Sá Carneiro", new Location("Norte", "Portugal", "Porto", new GPSCoordinates(41.0, -8.0)));
        destAirport = new Airport(new IATACode("MAD"), "Barajas", new Location("Madrid", "Espanha", "Madrid", new GPSCoordinates(40.0, -3.0)));
        
        RouteRequirement req = new RouteRequirement(500.0, 150);
        validRoute = new FlightRoute("teste-id-123", originAirport, destAirport, 300.0, 60, req, "Tester");
    }

    @Test
    void ensureCreateRouteReturns201Created() throws Exception {
        CreateFlightRouteDTO dto = new CreateFlightRouteDTO();
        dto.setOriginIata("OPO");
        dto.setDestinationIata("MAD");
        dto.setDistance(300.0);
        dto.setEstimatedFlightTime(60);
        dto.setMinRangeRequired(500.0);
        dto.setMinCapacityRequired(150);

        when(flightRouteService.createFlightRoute(any(CreateFlightRouteDTO.class))).thenReturn(validRoute);

        mockMvc.perform(post("/api/flight-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated()) 
                .andExpect(jsonPath("$.data.routeId").value("teste-id-123")) 
                .andExpect(jsonPath("$._links").exists()); 
    }

    @Test
    void ensureValidationFailsForNegativeDistanceReturns400() throws Exception {
        CreateFlightRouteDTO invalidDto = new CreateFlightRouteDTO();
        invalidDto.setOriginIata("OPO");
        invalidDto.setDestinationIata("MAD");
        invalidDto.setDistance(-100.0); 
        invalidDto.setEstimatedFlightTime(60);
        invalidDto.setMinRangeRequired(500.0);
        invalidDto.setMinCapacityRequired(150);

        mockMvc.perform(post("/api/flight-routes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.distance").exists()); 
    }

    @Test
    void ensureGetRouteByIdReturns200OK() throws Exception {
        when(flightRouteService.getRouteById("teste-id-123")).thenReturn(validRoute);

        mockMvc.perform(get("/api/flight-routes/teste-id-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.routeId").value("teste-id-123"));
    }
}