package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import lombok.Data;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService.MaintenanceTurnaroundSummary;

@Data
public class MaintenanceTurnaroundSummaryDTO {

    private String modelName;
    private Double avgDurationHours;

    public MaintenanceTurnaroundSummaryDTO(MaintenanceTurnaroundSummary summary) {
        this.modelName = summary.modelName();
        this.avgDurationHours = summary.avgDurationHours();
    }
}