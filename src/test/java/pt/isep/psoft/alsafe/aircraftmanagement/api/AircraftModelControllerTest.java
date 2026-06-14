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
import pt.isep.psoft.alsafe.aircraftmanagement.api.dto.UpdateAircraftModelDTO;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.services.AircraftModelService;
import pt.isep.psoft.alsafe.security.jwt.JwtUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(AircraftModelController.class)
@AutoConfigureMockMvc(addFilters = false) // Ignore security filters for unit test simplicity, but let's test roles if needed
class AircraftModelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AircraftModelService aircraftModelService;

    @MockBean
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private AircraftModel mockModel;

    @BeforeEach
    void setUp() throws Exception {
        mockModel = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);
        java.lang.reflect.Field idField = AircraftModel.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(mockModel, 1L);
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void ensureUpdateAircraftModelReturns200OK() throws Exception {
        UpdateAircraftModelDTO dto = new UpdateAircraftModelDTO(200, 27000.0, 6600.0, 850.0, 1L);

        when(aircraftModelService.updateAircraftModel(eq(1L), any(UpdateAircraftModelDTO.class))).thenReturn(mockModel);

        mockMvc.perform(put("/api/aircraft-models/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "BACKOFFICE_OPERATOR")
    void ensureUpdateAircraftModelReturns409ConflictOnVersionMismatch() throws Exception {
        UpdateAircraftModelDTO dto = new UpdateAircraftModelDTO(200, 27000.0, 6600.0, 850.0, 1L);

        when(aircraftModelService.updateAircraftModel(eq(1L), any(UpdateAircraftModelDTO.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(AircraftModel.class, 1L));

        mockMvc.perform(put("/api/aircraft-models/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }
}
