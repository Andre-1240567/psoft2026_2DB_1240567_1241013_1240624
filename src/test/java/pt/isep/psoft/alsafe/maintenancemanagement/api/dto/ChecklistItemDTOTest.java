package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.ChecklistItem;

import static org.junit.jupiter.api.Assertions.*;

class ChecklistItemDTOTest {

    @Test
    void ensureDtoCopiesDescriptionAndCompletedFromNotCompletedItem() {
        ChecklistItem item = new ChecklistItem("Check tyre pressure");

        ChecklistItemDTO dto = new ChecklistItemDTO(item);

        assertEquals("Check tyre pressure", dto.getDescription());
        assertFalse(dto.isCompleted());
    }

    @Test
    void ensureDtoReflectsCompletedItem() {
        ChecklistItem item = new ChecklistItem("Inspect cabin emergency equipment");
        item.markDone();

        ChecklistItemDTO dto = new ChecklistItemDTO(item);

        assertEquals("Inspect cabin emergency equipment", dto.getDescription());
        assertTrue(dto.isCompleted());
    }
}