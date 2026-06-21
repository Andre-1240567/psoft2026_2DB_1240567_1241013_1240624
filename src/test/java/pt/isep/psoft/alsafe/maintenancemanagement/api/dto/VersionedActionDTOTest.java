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

class VersionedActionDTOTest {

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

    @Test
    @DisplayName("a DTO with a non-null version has no violations")
    void validDtoHasNoViolations() {
        VersionedActionDTO dto = new VersionedActionDTO();
        dto.setVersion(0L);

        Set<ConstraintViolation<VersionedActionDTO>> violations = validator.validate(dto);

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("null version triggers @NotNull")
    void nullVersionIsRejected() {
        VersionedActionDTO dto = new VersionedActionDTO();
        dto.setVersion(null);

        Set<ConstraintViolation<VersionedActionDTO>> violations = validator.validate(dto);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString())
                .isEqualTo("version");
    }
}