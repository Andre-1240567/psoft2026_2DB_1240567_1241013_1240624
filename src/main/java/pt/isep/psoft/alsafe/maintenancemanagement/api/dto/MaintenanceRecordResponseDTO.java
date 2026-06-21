package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.hateoas.RepresentationModel;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceRecord;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class MaintenanceRecordResponseDTO extends RepresentationModel<MaintenanceRecordResponseDTO> {

    private Long id;
    private String registrationNumber;
    private String modelName;
    private Long templateId;
    private String templateName;
    private String description;
    private LocalDate startDate;
    private Double expectedDurationHours;
    private String status;
    private String component;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String completionNotes;
    private Double actualDurationHours;
    private Double estimatedCost;
    private Double actualCost;
    private LocalDate nextMaintenanceDueDate;
    private Double nextMaintenanceDueHours;
    private List<ChecklistItemDTO> checklist;
    private Long version;

    public MaintenanceRecordResponseDTO(MaintenanceRecord record) {
        this.id = record.getId();
        this.registrationNumber = record.getAircraft().getRegistrationNumber();
        this.modelName = record.getAircraft().getModel().getModelName();
        this.templateId = record.getTemplate().getId();
        this.templateName = record.getTemplate().getTemplateName();
        this.description = record.getDescription();
        this.startDate = record.getStartDate();
        this.expectedDurationHours = record.getExpectedDurationHours();
        this.status = record.getStatus().name();
        this.component = record.getComponent().name();
        this.createdAt = record.getCreatedAt();
        this.completedAt = record.getCompletedAt();
        this.completionNotes = record.getCompletionNotes();
        this.actualDurationHours = record.getActualDurationHours();
        this.estimatedCost = record.getEstimatedCost();
        this.actualCost = record.getActualCost();
        this.nextMaintenanceDueDate = record.getNextMaintenanceDueDate();
        this.nextMaintenanceDueHours = record.getNextMaintenanceDueHours();
        this.checklist = record.getChecklist().stream()
                .map(ChecklistItemDTO::new)
                .toList();
        this.version = record.getVersion();
    }
}