package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class UpdateMaintenanceTemplateDTO {

    private String templateName;

    private String templateType;

    @Positive(message = "Default duration must be strictly positive.")
    private Double defaultDurationHours;

    private List<Long> applicableModelIds;

    private List<String> checklistItems;

    @NotNull(message = "The (Optimistic Locking) version is mandatory.")
    private Long version;
}