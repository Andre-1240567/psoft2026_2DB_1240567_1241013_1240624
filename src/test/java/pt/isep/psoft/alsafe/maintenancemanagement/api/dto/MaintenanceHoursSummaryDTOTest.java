package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService.MaintenanceHoursSummary;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaintenanceHoursSummaryDTOTest {

    @Test
    void ensureDtoCopiesFieldsFromProjection() {
        MaintenanceHoursSummary summary = new MaintenanceHoursSummary("CS-TPA", 125.5);

        MaintenanceHoursSummaryDTO dto = new MaintenanceHoursSummaryDTO(summary);

        assertEquals("CS-TPA", dto.getRegistrationNumber());
        assertEquals(125.5, dto.getTotalHours());
    }
}