package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class CreateTerminalRequestDTO {
    @NotBlank(message = "Terminal designation is mandatory")
    private String designation;

    private List<String> gates;

    @Valid
    private List<CreateServiceDTO> services;
}
