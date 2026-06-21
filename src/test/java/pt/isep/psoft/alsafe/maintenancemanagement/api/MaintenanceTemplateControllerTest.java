package pt.isep.psoft.alsafe.maintenancemanagement.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.CreateMaintenanceTemplateDTO;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.UpdateMaintenanceTemplateDTO;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceTemplateService;
import pt.isep.psoft.alsafe.security.jwt.AuthTokenFilter;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaintenanceTemplateController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(MaintenanceTemplateModelAssembler.class)
class MaintenanceTemplateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MaintenanceTemplateService templateService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    private MaintenanceTemplate validTemplate;

    @BeforeEach
    void setUp() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);

        validTemplate = new MaintenanceTemplate(
                "A-Check Routine Inspection",
                TemplateType.INSPECTION,
                8.0,
                List.of(model),
                List.of("Visual inspection", "Check tyre pressure")
        );

        ReflectionTestUtils.setField(validTemplate, "id", 1L);
    }

    
    
    

    @Test
    void ensureCreateTemplateReturns201Created() throws Exception {
        CreateMaintenanceTemplateDTO dto = new CreateMaintenanceTemplateDTO();
        dto.setTemplateName("A-Check Routine Inspection");
        dto.setTemplateType("INSPECTION");
        dto.setDefaultDurationHours(8.0);
        dto.setApplicableModelIds(List.of(1L));
        dto.setChecklistItems(List.of("Visual inspection"));

        when(templateService.createTemplate(eq("A-Check Routine Inspection"), eq(TemplateType.INSPECTION),
                eq(8.0), eq(List.of(1L)), eq(List.of("Visual inspection"))))
                .thenReturn(validTemplate);

        mockMvc.perform(post("/api/maintenance-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.templateName").value("A-Check Routine Inspection"))
                .andExpect(jsonPath("$._links.self").exists())
                .andExpect(jsonPath("$._links.update").exists());
    }

    @Test
    void ensureCreateTemplateWithBlankNameReturns400() throws Exception {
        CreateMaintenanceTemplateDTO dto = new CreateMaintenanceTemplateDTO();
        dto.setTemplateName("");
        dto.setTemplateType("INSPECTION");
        dto.setDefaultDurationHours(8.0);
        dto.setApplicableModelIds(List.of(1L));
        dto.setChecklistItems(List.of("Visual inspection"));

        mockMvc.perform(post("/api/maintenance-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateTemplateWithInvalidTemplateTypeReturns400() throws Exception {
        CreateMaintenanceTemplateDTO dto = new CreateMaintenanceTemplateDTO();
        dto.setTemplateName("Unknown Type Template");
        dto.setTemplateType("NOT_A_REAL_TYPE");
        dto.setDefaultDurationHours(8.0);
        dto.setApplicableModelIds(List.of(1L));
        dto.setChecklistItems(List.of("Visual inspection"));

        mockMvc.perform(post("/api/maintenance-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateTemplateWithDuplicateNameReturns400() throws Exception {
        CreateMaintenanceTemplateDTO dto = new CreateMaintenanceTemplateDTO();
        dto.setTemplateName("A-Check Routine Inspection");
        dto.setTemplateType("INSPECTION");
        dto.setDefaultDurationHours(8.0);
        dto.setApplicableModelIds(List.of(1L));
        dto.setChecklistItems(List.of("Visual inspection"));

        when(templateService.createTemplate(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException(
                        "A maintenance template with name 'A-Check Routine Inspection' already exists."));

        mockMvc.perform(post("/api/maintenance-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureCreateTemplateWithUnknownModelReturns404() throws Exception {
        CreateMaintenanceTemplateDTO dto = new CreateMaintenanceTemplateDTO();
        dto.setTemplateName("New Template");
        dto.setTemplateType("INSPECTION");
        dto.setDefaultDurationHours(8.0);
        dto.setApplicableModelIds(List.of(999L));
        dto.setChecklistItems(List.of("Visual inspection"));

        when(templateService.createTemplate(any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Aircraft model with id '999' not found."));

        mockMvc.perform(post("/api/maintenance-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureCreateTemplateWithMissingChecklistReturns400() throws Exception {
        CreateMaintenanceTemplateDTO dto = new CreateMaintenanceTemplateDTO();
        dto.setTemplateName("New Template");
        dto.setTemplateType("INSPECTION");
        dto.setDefaultDurationHours(8.0);
        dto.setApplicableModelIds(List.of(1L));
        dto.setChecklistItems(List.of());

        mockMvc.perform(post("/api/maintenance-templates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    
    
    

    @Test
    void ensureGetTemplateByIdReturns200() throws Exception {
        when(templateService.getTemplateById(1L)).thenReturn(validTemplate);

        mockMvc.perform(get("/api/maintenance-templates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.templateName").value("A-Check Routine Inspection"))
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void ensureGetTemplateByIdReturns404WhenNotFound() throws Exception {
        when(templateService.getTemplateById(999L))
                .thenThrow(new ResourceNotFoundException("Maintenance template with id '999' not found."));

        mockMvc.perform(get("/api/maintenance-templates/999"))
                .andExpect(status().isNotFound());
    }

    
    
    

    @Test
    void ensureGetTemplatesWithoutFiltersReturnsAllTemplates() throws Exception {
        when(templateService.getAllTemplates()).thenReturn(List.of(validTemplate));

        mockMvc.perform(get("/api/maintenance-templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].templateName").value("A-Check Routine Inspection"));
    }

    @Test
    void ensureGetTemplatesFilteredByTypeReturnsMatching() throws Exception {
        when(templateService.getTemplatesByType(TemplateType.INSPECTION)).thenReturn(List.of(validTemplate));

        mockMvc.perform(get("/api/maintenance-templates").param("templateType", "INSPECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].templateType").value("INSPECTION"));
    }

    @Test
    void ensureGetTemplatesWithInvalidTypeFilterReturns400() throws Exception {
        mockMvc.perform(get("/api/maintenance-templates").param("templateType", "NOT_REAL"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureGetTemplatesFilteredByModelIdReturnsMatching() throws Exception {
        when(templateService.getTemplatesForModel(1L)).thenReturn(List.of(validTemplate));

        mockMvc.perform(get("/api/maintenance-templates").param("modelId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].templateName").value("A-Check Routine Inspection"));
    }

    @Test
    void ensureGetTemplatesFilteredByUnknownModelIdReturns404() throws Exception {
        when(templateService.getTemplatesForModel(999L))
                .thenThrow(new ResourceNotFoundException("Aircraft model with id '999' not found."));

        mockMvc.perform(get("/api/maintenance-templates").param("modelId", "999"))
                .andExpect(status().isNotFound());
    }

    
    
    

    @Test
    void ensureUpdateTemplateReturns200() throws Exception {
        UpdateMaintenanceTemplateDTO dto = new UpdateMaintenanceTemplateDTO();
        dto.setTemplateName("A-Check Updated");
        dto.setTemplateType("INSPECTION");
        dto.setDefaultDurationHours(10.0);
        dto.setVersion(0L);

        when(templateService.updateTemplate(eq(1L), eq(0L), eq("A-Check Updated"),
                eq(TemplateType.INSPECTION), eq(10.0), isNull(), isNull()))
                .thenReturn(validTemplate);

        mockMvc.perform(patch("/api/maintenance-templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self").exists());
    }

    @Test
    void ensureUpdateTemplateWithoutVersionReturns400() throws Exception {
        UpdateMaintenanceTemplateDTO dto = new UpdateMaintenanceTemplateDTO();
        dto.setTemplateName("A-Check Updated");

        mockMvc.perform(patch("/api/maintenance-templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureUpdateTemplateReturns404WhenNotFound() throws Exception {
        UpdateMaintenanceTemplateDTO dto = new UpdateMaintenanceTemplateDTO();
        dto.setVersion(0L);

        when(templateService.updateTemplate(eq(999L), any(), any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Maintenance template with id '999' not found."));

        mockMvc.perform(patch("/api/maintenance-templates/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void ensureUpdateTemplateWithOutdatedVersionReturns409Conflict() throws Exception {
        UpdateMaintenanceTemplateDTO dto = new UpdateMaintenanceTemplateDTO();
        dto.setVersion(0L);

        when(templateService.updateTemplate(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenThrow(new org.springframework.orm.ObjectOptimisticLockingFailureException(
                        MaintenanceTemplate.class, 1L));

        mockMvc.perform(patch("/api/maintenance-templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void ensureUpdateTemplateWithDuplicateNameReturns400() throws Exception {
        UpdateMaintenanceTemplateDTO dto = new UpdateMaintenanceTemplateDTO();
        dto.setTemplateName("Existing Name");
        dto.setVersion(0L);

        when(templateService.updateTemplate(eq(1L), any(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException(
                        "A maintenance template with name 'Existing Name' already exists."));

        mockMvc.perform(patch("/api/maintenance-templates/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ensureGetTemplatesWithBlankTypeReturnsAllTemplates() throws Exception {
        when(templateService.getAllTemplates()).thenReturn(List.of(validTemplate));

        mockMvc.perform(get("/api/maintenance-templates").param("templateType", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].templateName").value("A-Check Routine Inspection"));
    }
}