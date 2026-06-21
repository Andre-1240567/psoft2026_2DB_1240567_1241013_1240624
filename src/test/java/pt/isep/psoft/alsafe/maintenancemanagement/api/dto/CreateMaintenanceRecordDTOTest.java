package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateMaintenanceRecordDTOTest {

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

    private CreateMaintenanceRecordDTO buildValidDto() {
        CreateMaintenanceRecordDTO dto = new CreateMaintenanceRecordDTO();
        dto.setRegistrationNumber("CS-TTA");
        dto.setTemplateId(1L);
        dto.setDescription("Routine check");
        dto.setStartDate(LocalDate.now());
        dto.setComponent("AIRFRAME");
        // expectedDurationHours and estimatedCost are intentionally left null (optional).
        return dto;
    }

    @Test
    @DisplayName("a minimal valid DTO (optional fields null) has no violations")
    void minimalValidDtoHasNoViolations() {
        Set<ConstraintViolation<CreateMaintenanceRecordDTO>> violations = validator.validate(buildValidDto());

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("a fully populated DTO has no violations")
    void fullyPopulatedDtoHasNoViolations() {
        CreateMaintenanceRecordDTO dto = buildValidDto();
        dto.setExpectedDurationHours(10.0);
        dto.setEstimatedCost(500.0);

        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    @DisplayName("blank registrationNumber triggers @NotBlank")
    void blankRegistrationNumberIsRejected() {
        CreateMaintenanceRecordDTO dto = buildValidDto();
        dto.setRegistrationNumber(" ");

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("registrationNumber");
    }

    @Test
    @DisplayName("null templateId triggers @NotNull")
    void nullTemplateIdIsRejected() {
        CreateMaintenanceRecordDTO dto = buildValidDto();
        dto.setTemplateId(null);

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("templateId");
    }

    @Test
    @DisplayName("blank description triggers @NotBlank")
    void blankDescriptionIsRejected() {
        CreateMaintenanceRecordDTO dto = buildValidDto();
        dto.setDescription("");

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("description");
    }

    @Test
    @DisplayName("null startDate triggers @NotNull")
    void nullStartDateIsRejected() {
        CreateMaintenanceRecordDTO dto = buildValidDto();
        dto.setStartDate(null);

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("startDate");
    }

    @Test
    @DisplayName("blank component triggers @NotBlank")
    void blankComponentIsRejected() {
        CreateMaintenanceRecordDTO dto = buildValidDto();
        dto.setComponent("  ");

        assertThat(validator.validate(dto))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("component");
    }

    @Test
    @DisplayName("zero or negative expectedDurationHours triggers @Positive, when provided")
    void nonPositiveExpectedDurationIsRejectedWhenProvided() {
        CreateMaintenanceRecordDTO zero = buildValidDto();
        zero.setExpectedDurationHours(0.0);
        CreateMaintenanceRecordDTO negative = buildValidDto();
        negative.setExpectedDurationHours(-2.0);

        assertThat(validator.validate(zero))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("expectedDurationHours");
        assertThat(validator.validate(negative))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("expectedDurationHours");
    }

    @Test
    @DisplayName("negative estimatedCost triggers @PositiveOrZero, but zero is allowed")
    void negativeEstimatedCostIsRejectedButZeroIsAllowed() {
        CreateMaintenanceRecordDTO negative = buildValidDto();
        negative.setEstimatedCost(-1.0);
        CreateMaintenanceRecordDTO zero = buildValidDto();
        zero.setEstimatedCost(0.0);

        assertThat(validator.validate(negative))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("estimatedCost");
        assertThat(validator.validate(zero)).isEmpty();
    }
}