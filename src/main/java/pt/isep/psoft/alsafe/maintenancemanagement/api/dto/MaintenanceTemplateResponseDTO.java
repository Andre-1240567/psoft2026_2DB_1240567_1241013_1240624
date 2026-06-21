package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MaintenanceTemplateResponseDTO extends RepresentationModel<MaintenanceTemplateResponseDTO> {

    private Long id;
    private String templateName;
    private String templateType;
    private Double defaultDurationHours;
    private List<String> applicableModelNames;
    private List<ChecklistItemDTO> checklist;
    private Long version;

    public MaintenanceTemplateResponseDTO(MaintenanceTemplate template) {
        this.id = template.getId();
        this.templateName = template.getTemplateName();
        this.templateType = template.getTemplateType().name();
        this.defaultDurationHours = template.getDefaultDurationHours();
        this.applicableModelNames = template.getApplicableModels().stream()
                .map(AircraftModel::getModelName)
                .toList();
        this.checklist = template.getChecklist().stream()
                .map(ChecklistItemDTO::new)
                .toList();
        this.version = template.getVersion();
    }
}