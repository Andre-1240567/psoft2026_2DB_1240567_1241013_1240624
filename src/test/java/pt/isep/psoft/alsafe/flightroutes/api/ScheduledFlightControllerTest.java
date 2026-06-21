package pt.isep.psoft.alsafe.flightroutes.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.airportmanagement.domain.*;
import pt.isep.psoft.alsafe.flightroutes.api.dto.CreateScheduledFlightDTO;
import pt.isep.psoft.alsafe.flightroutes.domain.FlightRoute;
import pt.isep.psoft.alsafe.flightroutes.domain.RouteRequirement;
import pt.isep.psoft.alsafe.flightroutes.domain.ScheduledFlight;
import pt.isep.psoft.alsafe.flightroutes.services.ScheduledFlightService;
import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScheduledFlightController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ScheduledFlightModelAssembler.class)
class ScheduledFlightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ScheduledFlightService scheduledFlightService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    private ScheduledFlight validFlight;

    @BeforeEach
    void setUp() {
        Airport origin = new Airport(new IATACode("OPO"), "OPO", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        Airport dest = new Airport(new IATACode("MAD"), "MAD", new Location("R", "C", "C", new GPSCoordinates(0.0, 0.0)), new Timezone("UTC+00:00"));
        FlightRoute route = new FlightRoute("route123", origin, dest, 500.0, 60, new RouteRequirement(1000.0, 100), "atcc");

        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737", 150, 10000.0, 5000.0, 800.0);
        Aircraft aircraft = new Aircraft("CS-TPA", model, LocalDate.now().minusYears(1), "Economy");

        LocalDateTime departure = LocalDateTime.now().plusDays(1);
        LocalDateTime arrival = departure.plusHours(2);
        
        validFlight = new ScheduledFlight(route, aircraft, departure, arrival);
    }

    @Test
    void ensureScheduleFlightReturns201Created() throws Exception {
        CreateScheduledFlightDTO dto = new CreateScheduledFlightDTO();
        dto.setRouteId("route123");
        dto.setAircraftRegistration("CS-TPA");
        dto.setDepartureTime(LocalDateTime.now().plusDays(2));
        dto.setArrivalTime(LocalDateTime.now().plusDays(2).plusHours(2));

        when(scheduledFlightService.scheduleFlight(eq("route123"), eq("CS-TPA"), any(), any()))
                .thenReturn(validFlight);

        mockMvc.perform(post("/api/scheduled-flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routeId").value("route123"))
                .andExpect(jsonPath("$.aircraftRegistration").value("CS-TPA"))
                .andExpect(jsonPath("$._links").exists())
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.all-aircraft-flights").exists());
    }

    @Test
    void ensureScheduleFlightReturns400WhenDtoIsInvalid() throws Exception {
        CreateScheduledFlightDTO invalidDto = new CreateScheduledFlightDTO();
        invalidDto.setRouteId("route123"); 

        mockMvc.perform(post("/api/scheduled-flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureScheduleFlightReturns409WhenConflictOccurs() throws Exception {
        CreateScheduledFlightDTO dto = new CreateScheduledFlightDTO();
        dto.setRouteId("route123");
        dto.setAircraftRegistration("CS-TPA");
        dto.setDepartureTime(LocalDateTime.now().plusDays(2));
        dto.setArrivalTime(LocalDateTime.now().plusDays(2).plusHours(2));

        when(scheduledFlightService.scheduleFlight(eq("route123"), eq("CS-TPA"), any(), any()))
                .thenThrow(new IllegalStateException("Aircraft already scheduled."));

        mockMvc.perform(post("/api/scheduled-flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureScheduleFlightReturns404WhenAircraftOrRouteNotFound() throws Exception {
        CreateScheduledFlightDTO dto = new CreateScheduledFlightDTO();
        dto.setRouteId("UNKNOWN");
        dto.setAircraftRegistration("CS-TPA");
        dto.setDepartureTime(LocalDateTime.now().plusDays(2));
        dto.setArrivalTime(LocalDateTime.now().plusDays(2).plusHours(2));

        when(scheduledFlightService.scheduleFlight(eq("UNKNOWN"), eq("CS-TPA"), any(), any()))
                .thenThrow(new ResourceNotFoundException("Flight Route not found."));

        mockMvc.perform(post("/api/scheduled-flights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureGetFlightsByAircraftReturns200OK() throws Exception {
        when(scheduledFlightService.getScheduledFlightsByAircraft("CS-TPA"))
                .thenReturn(List.of(validFlight));

        mockMvc.perform(get("/api/scheduled-flights/aircraft/CS-TPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduledFlightResponseDTOList").exists())
                .andExpect(jsonPath("$._links.self").exists()); 
    }

    @Test
    void ensureGetFlightsByAircraftReturns404WhenAircraftDoesNotExist() throws Exception {
        when(scheduledFlightService.getScheduledFlightsByAircraft("UNKNOWN"))
                .thenThrow(new ResourceNotFoundException("Aircraft not found."));

        mockMvc.perform(get("/api/scheduled-flights/aircraft/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureGetFlightByIdReturns200OK() throws Exception {
        String flightNum = validFlight.getFlightNumber();

        when(scheduledFlightService.getFlightById(flightNum))
                .thenReturn(validFlight);

        mockMvc.perform(get("/api/scheduled-flights/" + flightNum))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value(flightNum))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void ensureGetFlightByIdReturns404WhenFlightDoesNotExist() throws Exception {
        when(scheduledFlightService.getFlightById("123-456"))
                .thenThrow(new ResourceNotFoundException("Scheduled flight not found."));

        mockMvc.perform(get("/api/scheduled-flights/123-456"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureCancelFlightReturns200OK() throws Exception {
        String flightNum = validFlight.getFlightNumber();
        
        validFlight.cancel();

        when(scheduledFlightService.cancelFlight(eq(flightNum))).thenReturn(validFlight);

        mockMvc.perform(patch("/api/scheduled-flights/" + flightNum + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flightNumber").value(flightNum))
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void ensureCancelFlightReturns409WhenAlreadyCanceled() throws Exception {
        when(scheduledFlightService.cancelFlight("123-456"))
                .thenThrow(new IllegalStateException("This flight is already canceled."));

        mockMvc.perform(patch("/api/scheduled-flights/123-456/cancel"))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureCancelFlightReturns404WhenNotFound() throws Exception {
        when(scheduledFlightService.cancelFlight("nonexistent"))
                .thenThrow(new ResourceNotFoundException("Scheduled flight not found."));

        mockMvc.perform(patch("/api/scheduled-flights/nonexistent/cancel"))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureGetUpcomingDeparturesReturns200OK() throws Exception {
        when(scheduledFlightService.getUpcomingDepartures(eq("OPO"), eq(24)))
                .thenReturn(List.of(validFlight));

        mockMvc.perform(get("/api/scheduled-flights/departures/OPO")
                        .param("hours", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].flightNumber").value(validFlight.getFlightNumber()))
                .andExpect(jsonPath("$[0].destinationIata").value("MAD"))
                .andExpect(jsonPath("$[0].aircraftModel").value("737"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }
}