package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import lombok.Data;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService.MaintenanceHoursSummary;

@Data
public class MaintenanceHoursSummaryDTO {

    private String registrationNumber;
    private Double totalHours;

    public MaintenanceHoursSummaryDTO(MaintenanceHoursSummary summary) {
        this.registrationNumber = summary.registrationNumber();
        this.totalHours = summary.totalHours();
    }
}