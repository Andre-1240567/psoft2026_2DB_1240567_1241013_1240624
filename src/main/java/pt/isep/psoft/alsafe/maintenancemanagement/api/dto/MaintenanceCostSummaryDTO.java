package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import lombok.Data;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService.MaintenanceCostSummary;

@Data
public class MaintenanceCostSummaryDTO {

    private String key;
    private Double estimatedCost;
    private Double actualCost;

    public MaintenanceCostSummaryDTO(MaintenanceCostSummary summary) {
        this.key = summary.key();
        this.estimatedCost = summary.estimatedCost();
        this.actualCost = summary.actualCost();
    }
}