package pt.isep.psoft.alsafe.maintenancemanagement.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;
import pt.isep.psoft.alsafe.aircraftmanagement.repositories.AircraftModelRepository;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.MaintenanceTemplate;
import pt.isep.psoft.alsafe.maintenancemanagement.domain.TemplateType;
import pt.isep.psoft.alsafe.maintenancemanagement.repositories.MaintenanceTemplateRepository;
import pt.isep.psoft.alsafe.shared.exceptions.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceTemplateServiceTest {

    @Mock
    private MaintenanceTemplateRepository templateRepository;

    @Mock
    private AircraftModelRepository aircraftModelRepository;

    @InjectMocks
    private MaintenanceTemplateService service;

    private AircraftModel a320;

    @BeforeEach
    void setUp() {
        a320 = new AircraftModel(Manufacturer.AIRBUS, "A320", 180, 24000.0, 6100.0, 828.0);
    }

    @Nested
    @DisplayName("createTemplate()")
    class CreateTemplateTests {

        @Test
        @DisplayName("creates and persists a valid template")
        void createsTemplate() {
            when(templateRepository.existsByTemplateName("A-Check")).thenReturn(false);
            when(aircraftModelRepository.findById(1L)).thenReturn(Optional.of(a320));
            when(templateRepository.save(any(MaintenanceTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceTemplate result = service.createTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0,
                    List.of(1L), List.of("Check oil level"));

            assertThat(result.getTemplateName()).isEqualTo("A-Check");
            assertThat(result.getApplicableModels()).containsExactly(a320);
            verify(templateRepository).save(any(MaintenanceTemplate.class));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when the template name already exists")
        void throwsWhenNameAlreadyExists() {
            when(templateRepository.existsByTemplateName("A-Check")).thenReturn(true);

            assertThatThrownBy(() -> service.createTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0,
                    List.of(1L), List.of("Check oil level")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            verify(templateRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when a model ID does not exist")
        void throwsWhenModelNotFound() {
            when(templateRepository.existsByTemplateName("A-Check")).thenReturn(false);
            when(aircraftModelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0,
                    List.of(99L), List.of("Check oil level")))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            verify(templateRepository, never()).save(any());
        }

        @Test
        @DisplayName("propagates domain validation errors (e.g. blank checklist)")
        void propagatesDomainValidation() {
            when(templateRepository.existsByTemplateName("A-Check")).thenReturn(false);
            when(aircraftModelRepository.findById(1L)).thenReturn(Optional.of(a320));

            assertThatThrownBy(() -> service.createTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0,
                    List.of(1L), List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("Query methods")
    class QueryTests {

        @Test
        @DisplayName("getAllTemplates() delegates to repository")
        void getAllTemplates() {
            MaintenanceTemplate template = buildTemplate();
            when(templateRepository.findAll()).thenReturn(List.of(template));

            List<MaintenanceTemplate> result = service.getAllTemplates();

            assertThat(result).containsExactly(template);
        }

        @Test
        @DisplayName("getTemplateById() returns the template when found")
        void getTemplateByIdFound() {
            MaintenanceTemplate template = buildTemplate();
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

            MaintenanceTemplate result = service.getTemplateById(1L);

            assertThat(result).isEqualTo(template);
        }

        @Test
        @DisplayName("getTemplateById() throws ResourceNotFoundException when missing")
        void getTemplateByIdNotFound() {
            when(templateRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getTemplateById(404L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("404");
        }

        @Test
        @DisplayName("getTemplatesByType() delegates to repository")
        void getTemplatesByType() {
            MaintenanceTemplate template = buildTemplate();
            when(templateRepository.findByTemplateType(TemplateType.INSPECTION))
                    .thenReturn(List.of(template));

            List<MaintenanceTemplate> result = service.getTemplatesByType(TemplateType.INSPECTION);

            assertThat(result).containsExactly(template);
        }

        @Test
        @DisplayName("getTemplatesForModel() validates the model exists, then delegates")
        void getTemplatesForModelFound() {
            MaintenanceTemplate template = buildTemplate();
            when(aircraftModelRepository.findById(1L)).thenReturn(Optional.of(a320));
            when(templateRepository.findByApplicableModel(1L)).thenReturn(List.of(template));

            List<MaintenanceTemplate> result = service.getTemplatesForModel(1L);

            assertThat(result).containsExactly(template);
        }

        @Test
        @DisplayName("getTemplatesForModel() throws ResourceNotFoundException when model is missing")
        void getTemplatesForModelNotFound() {
            when(aircraftModelRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getTemplatesForModel(99L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(templateRepository, never()).findByApplicableModel(any());
        }
    }

    @Nested
    @DisplayName("updateTemplate()")
    class UpdateTemplateTests {

        @Test
        @DisplayName("applies a partial update, keeping fields not provided")
        void appliesPartialUpdate() {
            MaintenanceTemplate template = buildTemplate();
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.save(any(MaintenanceTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            setVersion(template, 0L);

            MaintenanceTemplate result = service.updateTemplate(
                    1L, 0L, "Updated Name", null, null, null, null);

            assertThat(result.getTemplateName()).isEqualTo("Updated Name");
            assertThat(result.getTemplateType()).isEqualTo(TemplateType.INSPECTION);
        }

        @Test
        @DisplayName("updates the template type when a new one is provided")
        void updatesTemplateTypeWhenProvided() {
            MaintenanceTemplate template = buildTemplate();
            setVersion(template, 0L);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.save(any(MaintenanceTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceTemplate result = service.updateTemplate(
                    1L, 0L, null, TemplateType.OVERHAUL, null, null, null);

            assertThat(result.getTemplateType()).isEqualTo(TemplateType.OVERHAUL);
            assertThat(result.getTemplateName()).isEqualTo("A-Check");
        }

        @Test
        @DisplayName("updates the default duration when a new one is provided")
        void updatesDefaultDurationWhenProvided() {
            MaintenanceTemplate template = buildTemplate();
            setVersion(template, 0L);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.save(any(MaintenanceTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceTemplate result = service.updateTemplate(
                    1L, 0L, null, null, 16.0, null, null);

            assertThat(result.getDefaultDurationHours()).isEqualTo(16.0);
            assertThat(result.getTemplateType()).isEqualTo(TemplateType.INSPECTION);
        }

        @Test
        @DisplayName("updates all core fields at once when all are provided")
        void updatesAllCoreFieldsWhenAllProvided() {
            MaintenanceTemplate template = buildTemplate();
            setVersion(template, 0L);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.existsByTemplateName("Full Update")).thenReturn(false);
            when(templateRepository.save(any(MaintenanceTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceTemplate result = service.updateTemplate(
                    1L, 0L, "Full Update", TemplateType.MODIFICATION, 24.0, null, null);

            assertThat(result.getTemplateName()).isEqualTo("Full Update");
            assertThat(result.getTemplateType()).isEqualTo(TemplateType.MODIFICATION);
            assertThat(result.getDefaultDurationHours()).isEqualTo(24.0);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when the template does not exist")
        void throwsWhenTemplateNotFound() {
            when(templateRepository.findById(404L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateTemplate(
                    404L, 0L, "New Name", null, null, null, null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ObjectOptimisticLockingFailureException on version mismatch")
        void throwsOnVersionMismatch() {
            MaintenanceTemplate template = buildTemplate();
            setVersion(template, 5L);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));

            assertThatThrownBy(() -> service.updateTemplate(
                    1L, 1L, "New Name", null, null, null, null))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);

            verify(templateRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws IllegalArgumentException when changing to a name already taken")
        void throwsWhenNewNameAlreadyTaken() {
            MaintenanceTemplate template = buildTemplate();
            setVersion(template, 0L);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.existsByTemplateName("Taken")).thenReturn(true);

            assertThatThrownBy(() -> service.updateTemplate(
                    1L, 0L, "Taken", null, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");

            verify(templateRepository, never()).save(any());
        }

        @Test
        @DisplayName("does not check uniqueness when the name is unchanged")
        void doesNotCheckUniquenessWhenNameUnchanged() {
            MaintenanceTemplate template = buildTemplate();
            setVersion(template, 0L);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.save(any(MaintenanceTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            service.updateTemplate(1L, 0L, "A-Check", null, null, null, null);

            verify(templateRepository, never()).existsByTemplateName(anyString());
        }

        @Test
        @DisplayName("updates applicable models when provided")
        void updatesApplicableModels() {
            MaintenanceTemplate template = buildTemplate();
            setVersion(template, 0L);
            AircraftModel b737 = new AircraftModel(Manufacturer.BOEING, "737-800", 189, 26000.0, 5400.0, 842.0);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(aircraftModelRepository.findById(2L)).thenReturn(Optional.of(b737));
            when(templateRepository.save(any(MaintenanceTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceTemplate result = service.updateTemplate(
                    1L, 0L, null, null, null, List.of(2L), null);

            assertThat(result.getApplicableModels()).containsExactly(b737);
        }

        @Test
        @DisplayName("updates the checklist when provided")
        void updatesChecklist() {
            MaintenanceTemplate template = buildTemplate();
            setVersion(template, 0L);
            when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
            when(templateRepository.save(any(MaintenanceTemplate.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MaintenanceTemplate result = service.updateTemplate(
                    1L, 0L, null, null, null, null, List.of("New task"));

            assertThat(result.getChecklist()).extracting(item -> item.getDescription())
                    .containsExactly("New task");
        }
    }

    private MaintenanceTemplate buildTemplate() {
        return new MaintenanceTemplate(
                "A-Check", TemplateType.INSPECTION, 8.0,
                List.of(a320), List.of("Check oil level"));
    }

    private void setVersion(MaintenanceTemplate template, Long version) {
        try {
            var field = MaintenanceTemplate.class.getDeclaredField("version");
            field.setAccessible(true);
            field.set(template, version);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}