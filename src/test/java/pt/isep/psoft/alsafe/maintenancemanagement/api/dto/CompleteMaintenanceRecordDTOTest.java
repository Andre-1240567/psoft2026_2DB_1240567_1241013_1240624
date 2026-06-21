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

class CompleteMaintenanceRecordDTOTest {

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

    private CompleteMaintenanceRecordDTO buildValidDto() {
        CompleteMaintenanceRecordDTO dto = new CompleteMaintenanceRecordDTO();
        dto.setCompletionNotes("All checks passed");
        dto.setActualDurationHours(7.5);
        dto.setVersion(0L);
        // actualCost is intentionally left null (optional).
        return dto;
    }

    @Test
    @DisplayName("a minimal valid DTO (actualCost null) has no violations")
    void minimalValidDtoHasNoViolations() {
        Set<ConstraintViolation<CompleteMaintenanceRecordDTO>> violations = validator.validate(buildValidDto());

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("a fully populated DTO has no violations")
    void fullyPopulatedDtoHasNoViolations() {
        CompleteMaintenanceRecordDTO dto = buildValidDto();
        dto.setActualCost(480.0);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    @DisplayName("blank completionNotes triggers @NotBlank")
    void blankCompletionNotesIsRejected() {
        CompleteMaintenanceRecordDTO dto = buildValidDto();
        dto.setCompletionNotes("   ");

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("completionNotes");
    }

    @Test
    @DisplayName("null actualDurationHours triggers @NotNull")
    void nullActualDurationIsRejected() {
        CompleteMaintenanceRecordDTO dto = buildValidDto();
        dto.setActualDurationHours(null);

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("actualDurationHours");
    }

    @Test
    @DisplayName("zero or negative actualDurationHours triggers @Positive")
    void nonPositiveActualDurationIsRejected() {
        CompleteMaintenanceRecordDTO zero = buildValidDto();
        zero.setActualDurationHours(0.0);
        CompleteMaintenanceRecordDTO negative = buildValidDto();
        negative.setActualDurationHours(-3.0);

        assertThat(validator.validate(zero))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("actualDurationHours");
        assertThat(validator.validate(negative))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("actualDurationHours");
    }

    @Test
    @DisplayName("negative actualCost triggers @PositiveOrZero, but zero is allowed")
    void negativeActualCostIsRejectedButZeroIsAllowed() {
        CompleteMaintenanceRecordDTO negative = buildValidDto();
        negative.setActualCost(-10.0);
        CompleteMaintenanceRecordDTO zero = buildValidDto();
        zero.setActualCost(0.0);

        assertThat(validator.validate(negative))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("actualCost");
        assertThat(validator.validate(zero)).isEmpty();
    }

    @Test
    @DisplayName("null version triggers @NotNull")
    void nullVersionIsRejected() {
        CompleteMaintenanceRecordDTO dto = buildValidDto();
        dto.setVersion(null);

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("version");
    }
}