package pt.isep.psoft.alsafe.maintenancemanagement.api.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class CreateMaintenanceTemplateDTOTest {

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

    private CreateMaintenanceTemplateDTO buildValidDto() {
        CreateMaintenanceTemplateDTO dto = new CreateMaintenanceTemplateDTO();
        dto.setTemplateName("A-Check");
        dto.setTemplateType("INSPECTION");
        dto.setDefaultDurationHours(8.0);
        dto.setApplicableModelIds(List.of(1L));
        dto.setChecklistItems(List.of("Check oil level"));
        return dto;
    }

    @Test
    @DisplayName("a fully populated DTO has no violations")
    void validDtoHasNoViolations() {
        Set<ConstraintViolation<CreateMaintenanceTemplateDTO>> violations = validator.validate(buildValidDto());

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("blank templateName triggers @NotBlank")
    void blankTemplateNameIsRejected() {
        CreateMaintenanceTemplateDTO dto = buildValidDto();
        dto.setTemplateName("  ");

        Set<ConstraintViolation<CreateMaintenanceTemplateDTO>> violations = validator.validate(dto);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("templateName");
    }

    @Test
    @DisplayName("blank templateType triggers @NotBlank")
    void blankTemplateTypeIsRejected() {
        CreateMaintenanceTemplateDTO dto = buildValidDto();
        dto.setTemplateType("");

        Set<ConstraintViolation<CreateMaintenanceTemplateDTO>> violations = validator.validate(dto);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("templateType");
    }

    @Test
    @DisplayName("null defaultDurationHours triggers @NotNull")
    void nullDurationIsRejected() {
        CreateMaintenanceTemplateDTO dto = buildValidDto();
        dto.setDefaultDurationHours(null);

        Set<ConstraintViolation<CreateMaintenanceTemplateDTO>> violations = validator.validate(dto);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("defaultDurationHours");
    }

    @Test
    @DisplayName("zero or negative defaultDurationHours triggers @Positive")
    void nonPositiveDurationIsRejected() {
        CreateMaintenanceTemplateDTO zero = buildValidDto();
        zero.setDefaultDurationHours(0.0);
        CreateMaintenanceTemplateDTO negative = buildValidDto();
        negative.setDefaultDurationHours(-5.0);

        assertThat(validator.validate(zero))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("defaultDurationHours");
        assertThat(validator.validate(negative))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("defaultDurationHours");
    }

    @Test
    @DisplayName("null or empty applicableModelIds triggers @NotEmpty")
    void emptyApplicableModelIdsIsRejected() {
        CreateMaintenanceTemplateDTO nullList = buildValidDto();
        nullList.setApplicableModelIds(null);
        CreateMaintenanceTemplateDTO emptyList = buildValidDto();
        emptyList.setApplicableModelIds(List.of());

        assertThat(validator.validate(nullList))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("applicableModelIds");
        assertThat(validator.validate(emptyList))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("applicableModelIds");
    }

    @Test
    @DisplayName("null or empty checklistItems triggers @NotEmpty")
    void emptyChecklistItemsIsRejected() {
        CreateMaintenanceTemplateDTO nullList = buildValidDto();
        nullList.setChecklistItems(null);
        CreateMaintenanceTemplateDTO emptyList = buildValidDto();
        emptyList.setChecklistItems(List.of());

        assertThat(validator.validate(nullList))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("checklistItems");
        assertThat(validator.validate(emptyList))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("checklistItems");
    }

    @Test
    @DisplayName("a blank string inside checklistItems triggers the nested @NotBlank")
    void blankChecklistItemElementIsRejected() {
        CreateMaintenanceTemplateDTO dto = buildValidDto();
        dto.setChecklistItems(List.of("Valid item", "   "));

        Set<ConstraintViolation<CreateMaintenanceTemplateDTO>> violations = validator.validate(dto);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .anyMatch(path -> path.contains("checklistItems"));
    }
}