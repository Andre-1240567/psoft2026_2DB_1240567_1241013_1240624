package pt.isep.psoft.alsafe.maintenancemanagement.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaintenanceTemplateTest {

    private AircraftModel a320;
    private AircraftModel b737;
    private List<String> validChecklist;

    @BeforeEach
    void setUp() {
        a320 = new AircraftModel(Manufacturer.AIRBUS, "A320", 180, 24000.0, 6100.0, 828.0);
        b737 = new AircraftModel(Manufacturer.BOEING, "737-800", 189, 26000.0, 5400.0, 842.0);
        validChecklist = List.of("Check oil level", "Inspect landing gear");
    }

    
    
    

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorTests {

        @Test
        @DisplayName("creates a valid template with trimmed name, models, and checklist items")
        void createsValidTemplate() {
            MaintenanceTemplate template = new MaintenanceTemplate(
                    "  A-Check  ", TemplateType.INSPECTION, 8.0,
                    List.of(a320), validChecklist);

            assertThat(template.getTemplateName()).isEqualTo("A-Check");
            assertThat(template.getTemplateType()).isEqualTo(TemplateType.INSPECTION);
            assertThat(template.getDefaultDurationHours()).isEqualTo(8.0);
            assertThat(template.getApplicableModels()).containsExactly(a320);
            assertThat(template.getChecklist()).hasSize(2);
            assertThat(template.getChecklist().get(0).getDescription()).isEqualTo("Check oil level");
            assertThat(template.getChecklist().get(0).isCompleted()).isFalse();
        }

        @Test
        @DisplayName("rejects a blank (whitespace-only) template name")
        void rejectsBlankName() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "  ", TemplateType.INSPECTION, 8.0, List.of(a320), validChecklist))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("rejects a null template name")
        void rejectsNullName() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    null, TemplateType.INSPECTION, 8.0, List.of(a320), validChecklist))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("name");
        }

        @Test
        @DisplayName("rejects a null template type")
        void rejectsNullType() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "A-Check", null, 8.0, List.of(a320), validChecklist))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("type");
        }

        @Test
        @DisplayName("rejects a null default duration")
        void rejectsNullDuration() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, null, List.of(a320), validChecklist))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duration");
        }

        @Test
        @DisplayName("rejects a zero default duration")
        void rejectsZeroDuration() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 0.0, List.of(a320), validChecklist))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duration");
        }

        @Test
        @DisplayName("rejects a negative default duration")
        void rejectsNegativeDuration() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, -5.0, List.of(a320), validChecklist))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duration");
        }

        @Test
        @DisplayName("rejects a null applicable models list")
        void rejectsNullModels() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0, null, validChecklist))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("aircraft model");
        }

        @Test
        @DisplayName("rejects an empty applicable models list")
        void rejectsEmptyModelsList() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0, List.of(), validChecklist))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("aircraft model");
        }

        @Test
        @DisplayName("rejects a null checklist")
        void rejectsNullChecklist() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0, List.of(a320), null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("checklist item");
        }

        @Test
        @DisplayName("rejects an empty checklist")
        void rejectsEmptyChecklistList() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0, List.of(a320), List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("checklist item");
        }

        @Test
        @DisplayName("propagates ChecklistItem validation for blank descriptions")
        void propagatesChecklistItemValidation() {
            assertThatThrownBy(() -> new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0, List.of(a320), List.of("  ")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("blank");
        }
    }

    
    
    

    @Nested
    @DisplayName("updateDetails()")
    class UpdateDetailsTests {

        private MaintenanceTemplate template;

        @BeforeEach
        void init() {
            template = new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0, List.of(a320), validChecklist);
        }

        @Test
        @DisplayName("updates name, type and duration")
        void updatesDetails() {
            template.updateDetails("B-Check", TemplateType.SCHEDULED_MAINTENANCE, 16.0);

            assertThat(template.getTemplateName()).isEqualTo("B-Check");
            assertThat(template.getTemplateType()).isEqualTo(TemplateType.SCHEDULED_MAINTENANCE);
            assertThat(template.getDefaultDurationHours()).isEqualTo(16.0);
        }

        @Test
        @DisplayName("rejects a blank (empty) name on update")
        void rejectsBlankNameOnUpdate() {
            assertThatThrownBy(() -> template.updateDetails("", TemplateType.OVERHAUL, 10.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects a null name on update")
        void rejectsNullNameOnUpdate() {
            assertThatThrownBy(() -> template.updateDetails(null, TemplateType.OVERHAUL, 10.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects a null type on update")
        void rejectsNullTypeOnUpdate() {
            assertThatThrownBy(() -> template.updateDetails("A-Check", null, 10.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects a null duration on update")
        void rejectsNullDurationOnUpdate() {
            assertThatThrownBy(() -> template.updateDetails("A-Check", TemplateType.OVERHAUL, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects a zero duration on update")
        void rejectsZeroDurationOnUpdate() {
            assertThatThrownBy(() -> template.updateDetails("A-Check", TemplateType.OVERHAUL, 0.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects a negative duration on update")
        void rejectsInvalidDurationOnUpdate() {
            assertThatThrownBy(() -> template.updateDetails("A-Check", TemplateType.OVERHAUL, -1.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    
    
    

    @Nested
    @DisplayName("updateApplicableModels()")
    class UpdateApplicableModelsTests {

        private MaintenanceTemplate template;

        @BeforeEach
        void init() {
            template = new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0, List.of(a320), validChecklist);
        }

        @Test
        @DisplayName("replaces the applicable models list")
        void replacesModels() {
            template.updateApplicableModels(List.of(b737));

            assertThat(template.getApplicableModels()).containsExactly(b737);
        }

        @Test
        @DisplayName("rejects a null model list")
        void rejectsNullList() {
            assertThatThrownBy(() -> template.updateApplicableModels(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects an empty model list")
        void rejectsEmptyList() {
            assertThatThrownBy(() -> template.updateApplicableModels(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    
    
    

    @Nested
    @DisplayName("Checklist management")
    class ChecklistTests {

        private MaintenanceTemplate template;

        @BeforeEach
        void init() {
            template = new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0, List.of(a320), validChecklist);
        }

        @Test
        @DisplayName("replaceChecklist swaps all items")
        void replacesChecklist() {
            template.replaceChecklist(List.of("New task A", "New task B", "New task C"));

            assertThat(template.getChecklist()).hasSize(3);
            assertThat(template.getChecklist())
                    .extracting(ChecklistItem::getDescription)
                    .containsExactly("New task A", "New task B", "New task C");
        }

        @Test
        @DisplayName("rejects a null checklist on replace")
        void rejectsNullOnReplace() {
            assertThatThrownBy(() -> template.replaceChecklist(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects an empty checklist on replace")
        void rejectsEmptyOnReplace() {
            assertThatThrownBy(() -> template.replaceChecklist(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("getChecklist() returns an unmodifiable view")
        void getChecklistIsUnmodifiable() {
            List<ChecklistItem> checklist = template.getChecklist();

            assertThatThrownBy(() -> checklist.add(new ChecklistItem("Hack")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("cloneChecklist() returns independent copies, all not completed")
        void cloneChecklistIsIndependent() {
            
            template.getChecklist(); 
            
            
            List<ChecklistItem> clone1 = template.cloneChecklist();
            List<ChecklistItem> clone2 = template.cloneChecklist();

            assertThat(clone1).hasSize(2);
            assertThat(clone1.get(0)).isNotSameAs(template.getChecklist().get(0));
            assertThat(clone1.get(0)).isNotSameAs(clone2.get(0));

            
            clone1.get(0).markDone();

            assertThat(clone1.get(0).isCompleted()).isTrue();
            assertThat(clone2.get(0).isCompleted())
                    .as("clones must be fully independent of each other")
                    .isFalse();
            assertThat(template.getChecklist().get(0).isCompleted())
                    .as("the template master copy must never be affected by record working copies")
                    .isFalse();
        }
    }

    
    
    

    @Nested
    @DisplayName("isApplicableTo()")
    class IsApplicableToTests {

        private MaintenanceTemplate template;

        @BeforeEach
        void init() {
            template = new MaintenanceTemplate(
                    "A-Check", TemplateType.INSPECTION, 8.0, List.of(a320), validChecklist);
        }

        @Test
        @DisplayName("returns true when the model is in the applicable list")
        void returnsTrueForApplicableModel() {
            assertThat(template.isApplicableTo(a320)).isTrue();
        }

        @Test
        @DisplayName("returns false when the model is not in the applicable list")
        void returnsFalseForNonApplicableModel() {
            assertThat(template.isApplicableTo(b737)).isFalse();
        }

        @Test
        @DisplayName("returns false for a null model")
        void returnsFalseForNullModel() {
            assertThat(template.isApplicableTo(null)).isFalse();
        }
    }
}