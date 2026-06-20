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
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rolls back the database after each test
class AircraftModelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AircraftModelRepository aircraftModelRepository;

    @Autowired
    private pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftRepository aircraftRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // No deleteAll to avoid FK constraint issues with Bootstrap data
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void ensureIntegrationCreateAircraftModelWorks() throws Exception {
        CreateAircraftModelDTO dto = new CreateAircraftModelDTO();
        dto.setManufacturer(Manufacturer.BOEING);
        dto.setModelName("UniqueModel B1");
        dto.setSeatingCapacity(250);
        dto.setFuelCapacity(126000.0);
        dto.setMaxRange(13000.0);
        dto.setCruisingSpeed(903.0);

        mockMvc.perform(post("/api/aircraft-models")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.modelName").value("UniqueModel B1"));
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void ensureIntegrationGetAllAircraftModelsWorks() throws Exception {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "UniqueModel B2", 300, 150000.0, 14000.0, 905.0);
        aircraftModelRepository.save(model);

        mockMvc.perform(get("/api/aircraft-models")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[?(@.modelName == 'UniqueModel B2')]").exists());
    }
}
