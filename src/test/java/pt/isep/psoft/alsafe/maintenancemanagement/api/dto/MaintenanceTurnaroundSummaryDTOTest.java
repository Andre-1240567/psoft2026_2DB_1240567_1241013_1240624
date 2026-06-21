package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService.MaintenanceTurnaroundSummary;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaintenanceTurnaroundSummaryDTOTest {

    @Test
    void ensureDtoCopiesFieldsFromProjection() {
        MaintenanceTurnaroundSummary summary = new MaintenanceTurnaroundSummary("A320neo", 9.5);

        MaintenanceTurnaroundSummaryDTO dto = new MaintenanceTurnaroundSummaryDTO(summary);

        assertEquals("A320neo", dto.getModelName());
        assertEquals(9.5, dto.getAvgDurationHours());
    }
}