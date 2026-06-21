package pt.isep.psoft.alsafe.maintenancemanagement.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class MaintenanceTemplateRepositoryTest {

    @Autowired
    private MaintenanceTemplateRepository templateRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private AircraftModel a320;
    private AircraftModel b737;

    @BeforeEach
    void setUp() {
        a320 = new AircraftModel(Manufacturer.AIRBUS, "A320", 180, 24000.0, 6100.0, 828.0);
        b737 = new AircraftModel(Manufacturer.BOEING, "737-800", 189, 26000.0, 5400.0, 842.0);
        entityManager.persist(a320);
        entityManager.persist(b737);
        entityManager.flush();
    }

    @Nested
    @DisplayName("findByTemplateName() / existsByTemplateName()")
    class FindByTemplateNameTests {

        @Test
        @DisplayName("finds a template by its exact name")
        void findsByName() {
            MaintenanceTemplate template = persistTemplate("A-Check", TemplateType.INSPECTION, List.of(a320));

            Optional<MaintenanceTemplate> result = templateRepository.findByTemplateName("A-Check");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(template.getId());
        }

        @Test
        @DisplayName("returns empty when no template matches the name")
        void returnsEmptyWhenNotFound() {
            assertThat(templateRepository.findByTemplateName("Nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("existsByTemplateName() returns true/false correctly")
        void existsByTemplateName() {
            persistTemplate("A-Check", TemplateType.INSPECTION, List.of(a320));

            assertThat(templateRepository.existsByTemplateName("A-Check")).isTrue();
            assertThat(templateRepository.existsByTemplateName("B-Check")).isFalse();
        }
    }

    @Nested
    @DisplayName("findByTemplateType()")
    class FindByTemplateTypeTests {

        @Test
        @DisplayName("returns only templates matching the given type")
        void filtersByType() {
            persistTemplate("A-Check", TemplateType.INSPECTION, List.of(a320));
            persistTemplate("Overhaul-1", TemplateType.OVERHAUL, List.of(a320));

            List<MaintenanceTemplate> result = templateRepository.findByTemplateType(TemplateType.INSPECTION);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTemplateName()).isEqualTo("A-Check");
        }

        @Test
        @DisplayName("returns an empty list when no template matches the type")
        void returnsEmptyWhenNoMatch() {
            persistTemplate("A-Check", TemplateType.INSPECTION, List.of(a320));

            assertThat(templateRepository.findByTemplateType(TemplateType.MODIFICATION)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByApplicableModel()")
    class FindByApplicableModelTests {

        @Test
        @DisplayName("returns templates applicable to the given model ID")
        void returnsApplicableTemplates() {
            persistTemplate("A-Check", TemplateType.INSPECTION, List.of(a320));
            persistTemplate("B-Check", TemplateType.SCHEDULED_MAINTENANCE, List.of(b737));
            persistTemplate("Shared-Check", TemplateType.OVERHAUL, List.of(a320, b737));

            List<MaintenanceTemplate> result = templateRepository.findByApplicableModel(a320.getId());

            assertThat(result)
                    .extracting(MaintenanceTemplate::getTemplateName)
                    .containsExactlyInAnyOrder("A-Check", "Shared-Check");
        }

        @Test
        @DisplayName("returns an empty list when the model has no applicable templates")
        void returnsEmptyWhenNoneApplicable() {
            persistTemplate("B-Check", TemplateType.SCHEDULED_MAINTENANCE, List.of(b737));

            assertThat(templateRepository.findByApplicableModel(a320.getId())).isEmpty();
        }
    }

    
    
    

    private MaintenanceTemplate persistTemplate(String name, TemplateType type, List<AircraftModel> models) {
        MaintenanceTemplate template = new MaintenanceTemplate(
                name, type, 8.0, models, List.of("Default checklist item"));
        return templateRepository.save(template);
    }
}