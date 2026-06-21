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

class UpdateMaintenanceTemplateDTOTest {

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

    private UpdateMaintenanceTemplateDTO buildMinimalValidDto() {
        UpdateMaintenanceTemplateDTO dto = new UpdateMaintenanceTemplateDTO();
        dto.setVersion(0L);
        return dto;
    }

    @Test
    @DisplayName("a DTO with only 'version' set (all other fields null) has no violations")
    void minimalDtoWithOnlyVersionIsValid() {
        Set<ConstraintViolation<UpdateMaintenanceTemplateDTO>> violations =
                validator.validate(buildMinimalValidDto());

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("a fully populated DTO has no violations")
    void fullyPopulatedDtoIsValid() {
        UpdateMaintenanceTemplateDTO dto = buildMinimalValidDto();
        dto.setTemplateName("B-Check");
        dto.setTemplateType("SCHEDULED_MAINTENANCE");
        dto.setDefaultDurationHours(16.0);
        dto.setApplicableModelIds(List.of(1L, 2L));
        dto.setChecklistItems(List.of("New task"));

        assertThat(validator.validate(dto)).isEmpty();
    }

    @Test
    @DisplayName("null version triggers @NotNull (the only mandatory field)")
    void nullVersionIsRejected() {
        UpdateMaintenanceTemplateDTO dto = buildMinimalValidDto();
        dto.setVersion(null);

        Set<ConstraintViolation<UpdateMaintenanceTemplateDTO>> violations = validator.validate(dto);

        assertThat(violations).extracting(v -> v.getPropertyPath().toString())
                .contains("version");
    }

    @Test
    @DisplayName("zero or negative defaultDurationHours triggers @Positive, when provided")
    void nonPositiveDurationIsRejectedWhenProvided() {
        UpdateMaintenanceTemplateDTO zero = buildMinimalValidDto();
        zero.setDefaultDurationHours(0.0);
        UpdateMaintenanceTemplateDTO negative = buildMinimalValidDto();
        negative.setDefaultDurationHours(-1.0);

        assertThat(validator.validate(zero))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("defaultDurationHours");
        assertThat(validator.validate(negative))
                .extracting(v -> v.getPropertyPath().toString())
                .contains("defaultDurationHours");
    }

    @Test
    @DisplayName("templateName, templateType, applicableModelIds and checklistItems have no constraints — null is valid")
    void optionalFieldsHaveNoConstraints() {
        UpdateMaintenanceTemplateDTO dto = buildMinimalValidDto();
        dto.setTemplateName(null);
        dto.setTemplateType(null);
        dto.setApplicableModelIds(null);
        dto.setChecklistItems(null);

        assertThat(validator.validate(dto)).isEmpty();
    }
}