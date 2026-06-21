package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CancelMaintenanceRecordDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    private CancelMaintenanceRecordDTO buildValidDto() {
        CancelMaintenanceRecordDTO dto = new CancelMaintenanceRecordDTO();
        dto.setReason("Aircraft reassigned");
        dto.setVersion(0L);
        return dto;
    }

    @Test
    @DisplayName("a fully populated DTO has no violations")
    void validDtoHasNoViolations() {
        Set<ConstraintViolation<CancelMaintenanceRecordDTO>> violations = validator.validate(buildValidDto());

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("blank reason triggers @NotBlank")
    void blankReasonIsRejected() {
        CancelMaintenanceRecordDTO dto = buildValidDto();
        dto.setReason("");

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("reason");
    }

    @Test
    @DisplayName("null reason triggers @NotBlank")
    void nullReasonIsRejected() {
        CancelMaintenanceRecordDTO dto = buildValidDto();
        dto.setReason(null);

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("reason");
    }

    @Test
    @DisplayName("null version triggers @NotNull")
    void nullVersionIsRejected() {
        CancelMaintenanceRecordDTO dto = buildValidDto();
        dto.setVersion(null);

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("version");
    }

    @Test
    @DisplayName("both reason and version invalid produces two distinct violations")
    void bothFieldsInvalidProducesTwoViolations() {
        CancelMaintenanceRecordDTO dto = buildValidDto();
        dto.setReason(" ");
        dto.setVersion(null);

        Set<ConstraintViolation<CancelMaintenanceRecordDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(2);
        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .containsExactlyInAnyOrder("reason", "version");
    }
}