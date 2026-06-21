package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import lombok.Data;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.ChecklistItem;

@Data
public class ChecklistItemDTO {

    private String description;
    private boolean completed;

    public ChecklistItemDTO(ChecklistItem item) {
        this.description = item.getDescription();
        this.completed = item.isCompleted();
    }
}