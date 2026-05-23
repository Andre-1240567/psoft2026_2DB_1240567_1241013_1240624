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
}