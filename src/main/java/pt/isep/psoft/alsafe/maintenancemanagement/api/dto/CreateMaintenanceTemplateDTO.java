package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class CreateMaintenanceTemplateDTO {

    @NotBlank(message = "Template name is mandatory.")
    private String templateName;

    @NotBlank(message = "Template type is mandatory.")
    private String templateType;

    @NotNull(message = "Default duration is mandatory.")
    @Positive(message = "Default duration must be strictly positive.")
    private Double defaultDurationHours;

    @NotEmpty(message = "At least one applicable aircraft model must be specified.")
    private List<Long> applicableModelIds;

    @NotEmpty(message = "At least one checklist item is required.")
    private List<@NotBlank(message = "Checklist item description cannot be blank.") String> checklistItems;
}