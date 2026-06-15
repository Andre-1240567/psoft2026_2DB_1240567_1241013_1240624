package pt.isep.psoft.alsafe.airportmanagement.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import pt.isep.psoft.alsafe.airportmanagement.domain.ContactType;

@Getter
@Setter
public class ContactDTO {
    @NotBlank
    @Schema(example = "+351 22 123 4567")
    private String value;

    @Schema(example = "Information Desk")
    private String department;

    @NotNull
    @Schema(example = "PHONE")
    private ContactType type;
}
