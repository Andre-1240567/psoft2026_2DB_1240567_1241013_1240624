package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MaintenanceTemplateResponseDTOTest {

    private AircraftModel createModel(String name) {
        return new AircraftModel(Manufacturer.BOEING, name, 180, 26000.0, 6500.0, 840.0);
    }

    @Test
    void ensureDtoMapsAllFieldsFromTemplate() {
        AircraftModel model1 = createModel("737 MAX");
        AircraftModel model2 = createModel("A320neo");

        MaintenanceTemplate template = new MaintenanceTemplate(
                "A-Check Routine Inspection",
                TemplateType.INSPECTION,
                8.0,
                List.of(model1, model2),
                List.of("Visual inspection", "Check tyre pressure")
        );

        MaintenanceTemplateResponseDTO dto = new MaintenanceTemplateResponseDTO(template);

        assertEquals(template.getId(), dto.getId());
        assertEquals("A-Check Routine Inspection", dto.getTemplateName());
        assertEquals("INSPECTION", dto.getTemplateType());
        assertEquals(8.0, dto.getDefaultDurationHours());
        assertEquals(List.of("737 MAX", "A320neo"), dto.getApplicableModelNames());
        assertEquals(2, dto.getChecklist().size());
        assertEquals("Visual inspection", dto.getChecklist().get(0).getDescription());
        assertFalse(dto.getChecklist().get(0).isCompleted());
    }

    @Test
    void ensureDtoExposesTemplateVersionForOptimisticLocking() {
        AircraftModel model = createModel("777X");

        MaintenanceTemplate template = new MaintenanceTemplate(
                "Engine Overhaul",
                TemplateType.OVERHAUL,
                160.0,
                List.of(model),
                List.of("Full engine disassembly")
        );

        MaintenanceTemplateResponseDTO dto = new MaintenanceTemplateResponseDTO(template);

        assertEquals(template.getVersion(), dto.getVersion());
    }
}