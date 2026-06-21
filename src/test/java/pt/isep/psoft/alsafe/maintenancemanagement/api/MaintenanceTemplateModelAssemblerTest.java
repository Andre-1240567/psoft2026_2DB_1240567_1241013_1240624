package pt.isep.psoft.alsafe.maintenancemanagement.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.maintenancemanagement.api.dto.MaintenanceTemplateResponseDTO;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceTemplateModelAssemblerTest {

    private final MaintenanceTemplateModelAssembler assembler = new MaintenanceTemplateModelAssembler();

    @BeforeEach
    void setUpRequestContext() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDownRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    private MaintenanceTemplate createTemplate() {
        AircraftModel model = new AircraftModel(Manufacturer.BOEING, "737 MAX", 180, 26000.0, 6500.0, 840.0);

        MaintenanceTemplate template = new MaintenanceTemplate(
                "A-Check Routine Inspection",
                TemplateType.INSPECTION,
                8.0,
                List.of(model),
                List.of("Visual inspection")
        );

        ReflectionTestUtils.setField(template, "id", 1L);
        return template;
    }

    @Test
    void ensureToModelMapsCoreFields() {
        MaintenanceTemplate template = createTemplate();

        MaintenanceTemplateResponseDTO dto = assembler.toModel(template);

        assertEquals("A-Check Routine Inspection", dto.getTemplateName());
        assertEquals("INSPECTION", dto.getTemplateType());
    }

    @Test
    void ensureToModelAddsSelfLink() {
        MaintenanceTemplate template = createTemplate();

        MaintenanceTemplateResponseDTO dto = assembler.toModel(template);

        assertTrue(dto.getLink("self").isPresent());
        assertTrue(dto.getLink("self").get().getHref().contains("/api/maintenance-templates/1"));
    }

    @Test
    void ensureToModelAddsUpdateLink() {
        MaintenanceTemplate template = createTemplate();

        MaintenanceTemplateResponseDTO dto = assembler.toModel(template);

        assertTrue(dto.getLink("update").isPresent());
        assertTrue(dto.getLink("update").get().getHref().contains("/api/maintenance-templates/1"));
    }
}