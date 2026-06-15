package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateServiceDTO {
    @NotBlank(message = "Service type is mandatory")
    private String serviceType;
    
    private String description;
}
