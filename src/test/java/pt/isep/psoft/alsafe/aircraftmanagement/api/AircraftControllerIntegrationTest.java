package pt.isep.psoft.alsafe.aircraftmanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftStatus;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back the database after each test
class AircraftControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AircraftRepository aircraftRepository;

    @Autowired
    private AircraftModelRepository aircraftModelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        AircraftModel model = new AircraftModel(Manufacturer.AIRBUS, "UniqueModel A1", 300, 150000.0, 15000.0, 903.0);
        model = aircraftModelRepository.save(model);

        Aircraft aircraft = new Aircraft("CS-ZZA", model, LocalDate.now(), "Economy");
        aircraft.updateStatus(AircraftStatus.AVAILABLE);
        aircraftRepository.save(aircraft);
    }

    @Test
    @WithMockUser(roles = "ATCC") // User roles required by security config
    void ensureIntegrationCreateAircraftWorks() throws Exception {
        CreateAircraftDTO dto = new CreateAircraftDTO();
        dto.setRegistrationNumber("CS-ZZB");
        dto.setModelName("UniqueModel A1");
        dto.setManufacturingDate(LocalDate.now());
        dto.setActiveConfigurationName("Economy");

        mockMvc.perform(post("/api/aircrafts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.registrationNumber").value("CS-ZZB"));
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void ensureIntegrationGetAircraftDetailsWorks() throws Exception {
        mockMvc.perform(get("/api/aircrafts/CS-ZZA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registrationNumber").value("CS-ZZA"))
                .andExpect(jsonPath("$.modelName").value("UniqueModel A1"));
    }

    @Test
    @WithMockUser(roles = "ATCC")
    void ensureIntegrationSearchAircraftsWorks() throws Exception {
        mockMvc.perform(get("/api/aircrafts")
                        .param("modelName", "UniqueModel A1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].registrationNumber").value("CS-ZZA"));
    }
}
