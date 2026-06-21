package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.maintenancemanagement.services.MaintenanceRecordService.MaintenanceCostSummary;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MaintenanceCostSummaryDTOTest {

    @Test
    void ensureDtoCopiesFieldsFromProjectionWithBothCosts() {
        MaintenanceCostSummary summary = new MaintenanceCostSummary("CS-TPD", 9000.0, 9650.0);

        MaintenanceCostSummaryDTO dto = new MaintenanceCostSummaryDTO(summary);

        assertEquals("CS-TPD", dto.getKey());
        assertEquals(9000.0, dto.getEstimatedCost());
        assertEquals(9650.0, dto.getActualCost());
    }

    @Test
    void ensureDtoHandlesNullCostsFromProjection() {
        MaintenanceCostSummary summary = new MaintenanceCostSummary("777X", null, null);

        MaintenanceCostSummaryDTO dto = new MaintenanceCostSummaryDTO(summary);

        assertEquals("777X", dto.getKey());
        assertNull(dto.getEstimatedCost());
        assertNull(dto.getActualCost());
    }
}