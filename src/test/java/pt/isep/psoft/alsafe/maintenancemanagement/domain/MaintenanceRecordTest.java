package pt.isep.psoft.alsafe.maintenancemanagement.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Aircraft;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.AircraftModel;
import pt.isep.psoft.alsafe.aircraftmanagement.domain.Manufacturer;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MaintenanceRecordTest {

    private AircraftModel a320;
    private AircraftModel b737;
    private Aircraft aircraft;
    private MaintenanceTemplate templateForA320;
    private MaintenanceTemplate templateForB737;

    @BeforeEach
    void setUp() {
        a320 = new AircraftModel(Manufacturer.AIRBUS, "A320", 180, 24000.0, 6100.0, 828.0);
        b737 = new AircraftModel(Manufacturer.BOEING, "737-800", 189, 26000.0, 5400.0, 842.0);

        aircraft = new Aircraft("CS-TTA", a320, LocalDate.of(2018, 5, 10), "Standard");

        templateForA320 = new MaintenanceTemplate(
                "A-Check", TemplateType.INSPECTION, 8.0,
                List.of(a320), List.of("Check oil level", "Inspect landing gear"));

        templateForB737 = new MaintenanceTemplate(
                "B-Check", TemplateType.SCHEDULED_MAINTENANCE, 16.0,
                List.of(b737), List.of("Engine borescope"));
    }

    
    
    

    @Nested
    @DisplayName("Constructor validation")
    class ConstructorTests {

        @Test
        @DisplayName("creates a valid PLANNED record, deep-copying the template checklist")
        void createsValidRecord() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "  Routine A-Check  ",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, 500.0);

            assertThat(record.getAircraft()).isEqualTo(aircraft);
            assertThat(record.getTemplate()).isEqualTo(templateForA320);
            assertThat(record.getDescription()).isEqualTo("Routine A-Check");
            assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.PLANNED);
            assertThat(record.getCreatedAt()).isNotNull();
            assertThat(record.getComponent()).isEqualTo(MaintenanceComponent.AIRFRAME);
            assertThat(record.getEstimatedCost()).isEqualTo(500.0);
            assertThat(record.getChecklist()).hasSize(2);
        }

        @Test
        @DisplayName("uses the template's default duration when none is provided")
        void usesTemplateDefaultDuration() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            assertThat(record.getExpectedDurationHours()).isEqualTo(8.0);
        }

        @Test
        @DisplayName("uses the provided duration when positive, overriding the template default")
        void usesProvidedDurationWhenPositive() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), 12.5, MaintenanceComponent.AIRFRAME, null);

            assertThat(record.getExpectedDurationHours()).isEqualTo(12.5);
        }

        @Test
        @DisplayName("falls back to template default when provided duration is zero or negative")
        void fallsBackToDefaultWhenDurationNonPositive() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), -3.0, MaintenanceComponent.AIRFRAME, null);

            assertThat(record.getExpectedDurationHours()).isEqualTo(8.0);
        }

        @Test
        @DisplayName("the record's checklist is an independent copy from the template's master copy")
        void checklistIsIndependentCopy() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            record.completeChecklistItem(0);

            assertThat(record.getChecklist().get(0).isCompleted()).isTrue();
            assertThat(templateForA320.getChecklist().get(0).isCompleted())
                    .as("completing a record's checklist item must never affect the template master copy")
                    .isFalse();
        }

        @Test
        @DisplayName("rejects a null aircraft")
        void rejectsNullAircraft() {
            assertThatThrownBy(() -> new MaintenanceRecord(
                    null, templateForA320, "Desc", LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Aircraft");
        }

        @Test
        @DisplayName("rejects a null template")
        void rejectsNullTemplate() {
            assertThatThrownBy(() -> new MaintenanceRecord(
                    aircraft, null, "Desc", LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("template");
        }

        @Test
        @DisplayName("rejects a blank (whitespace-only) description")
        void rejectsBlankDescription() {
            assertThatThrownBy(() -> new MaintenanceRecord(
                    aircraft, templateForA320, "   ", LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Description");
        }

        @Test
        @DisplayName("rejects a null description")
        void rejectsNullDescription() {
            assertThatThrownBy(() -> new MaintenanceRecord(
                    aircraft, templateForA320, null, LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Description");
        }

        @Test
        @DisplayName("rejects a null start date")
        void rejectsNullStartDate() {
            assertThatThrownBy(() -> new MaintenanceRecord(
                    aircraft, templateForA320, "Desc", null, null,
                    MaintenanceComponent.AIRFRAME, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Start date");
        }

        @Test
        @DisplayName("rejects a null maintenance component")
        void rejectsNullComponent() {
            assertThatThrownBy(() -> new MaintenanceRecord(
                    aircraft, templateForA320, "Desc", LocalDate.now(), null,
                    null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("component");
        }

        @Test
        @DisplayName("rejects a negative estimated cost")
        void rejectsNegativeEstimatedCost() {
            assertThatThrownBy(() -> new MaintenanceRecord(
                    aircraft, templateForA320, "Desc", LocalDate.now(), null,
                    MaintenanceComponent.AIRFRAME, -10.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Estimated cost");
        }

        @Test
        @DisplayName("rejects a template that is not applicable to the aircraft's model")
        void rejectsTemplateNotApplicableToModel() {
            
            assertThatThrownBy(() -> new MaintenanceRecord(
                    aircraft, templateForB737, "Desc", LocalDate.now(), null,
                    MaintenanceComponent.ENGINE, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not applicable");
        }
    }

    
    
    

    @Nested
    @DisplayName("startWork()")
    class StartWorkTests {

        private MaintenanceRecord record;

        @BeforeEach
        void init() {
            record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
        }

        @Test
        @DisplayName("transitions PLANNED -> IN_PROGRESS")
        void transitionsToInProgress() {
            record.startWork();

            assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("throws IllegalStateException if not PLANNED")
        void throwsIfNotPlanned() {
            record.startWork();

            assertThatThrownBy(record::startWork)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("IN_PROGRESS");
        }
    }

    
    
    

    @Nested
    @DisplayName("markAsCompleted()")
    class MarkAsCompletedTests {

        private MaintenanceRecord record;

        @BeforeEach
        void init() {
            record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, 500.0);
            record.startWork();
        }

        @Test
        @DisplayName("transitions IN_PROGRESS -> COMPLETED with notes, duration, and cost")
        void completesSuccessfully() {
            record.markAsCompleted("All checks passed", 7.5, 480.0);

            assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.COMPLETED);
            assertThat(record.getCompletionNotes()).isEqualTo("All checks passed");
            assertThat(record.getActualDurationHours()).isEqualTo(7.5);
            assertThat(record.getActualCost()).isEqualTo(480.0);
            assertThat(record.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("overload without cost completes successfully with null actual cost")
        void completesWithOverload() {
            record.markAsCompleted("All checks passed", 7.5);

            assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.COMPLETED);
            assertThat(record.getActualCost()).isNull();
        }

        @Test
        @DisplayName("throws IllegalStateException if not IN_PROGRESS")
        void throwsIfNotInProgress() {
            MaintenanceRecord planned = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            assertThatThrownBy(() -> planned.markAsCompleted("notes", 5.0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PLANNED");
        }

        @Test
        @DisplayName("rejects blank (whitespace-only) completion notes")
        void rejectsBlankNotes() {
            assertThatThrownBy(() -> record.markAsCompleted("  ", 5.0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("notes");
        }

        @Test
        @DisplayName("rejects null completion notes")
        void rejectsNullNotes() {
            assertThatThrownBy(() -> record.markAsCompleted(null, 5.0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("notes");
        }

        @Test
        @DisplayName("rejects a null actual duration")
        void rejectsNullActualDuration() {
            assertThatThrownBy(() -> record.markAsCompleted("notes", null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duration");
        }

        @Test
        @DisplayName("rejects a zero actual duration")
        void rejectsZeroActualDuration() {
            assertThatThrownBy(() -> record.markAsCompleted("notes", 0.0, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("duration");
        }

        @Test
        @DisplayName("rejects a negative actual cost")
        void rejectsNegativeActualCost() {
            assertThatThrownBy(() -> record.markAsCompleted("notes", 5.0, -1.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cost");
        }
    }

    
    
    

    @Nested
    @DisplayName("cancel()")
    class CancelTests {

        @Test
        @DisplayName("cancels a PLANNED record")
        void cancelsPlannedRecord() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            record.cancel("Aircraft reassigned");

            assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.CANCELED);
            assertThat(record.getCompletionNotes()).contains("CANCELED").contains("Aircraft reassigned");
            assertThat(record.getCompletedAt()).isNotNull();
        }

        @Test
        @DisplayName("cancels an IN_PROGRESS record")
        void cancelsInProgressRecord() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
            record.startWork();

            record.cancel("Parts unavailable");

            assertThat(record.getStatus()).isEqualTo(MaintenanceStatus.CANCELED);
        }

        @Test
        @DisplayName("throws IllegalStateException when already COMPLETED")
        void throwsWhenAlreadyCompleted() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
            record.startWork();
            record.markAsCompleted("Done", 5.0);

            assertThatThrownBy(() -> record.cancel("Too late"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("throws IllegalStateException when already CANCELED")
        void throwsWhenAlreadyCanceled() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
            record.cancel("First reason");

            assertThatThrownBy(() -> record.cancel("Second reason"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("CANCELED");
        }

        @Test
        @DisplayName("rejects a blank (whitespace-only) cancellation reason")
        void rejectsBlankReason() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            assertThatThrownBy(() -> record.cancel("  "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reason");
        }

        @Test
        @DisplayName("rejects a null cancellation reason")
        void rejectsNullReason() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            assertThatThrownBy(() -> record.cancel(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("reason");
        }
    }

    
    
    

    @Nested
    @DisplayName("scheduleNextMaintenance()")
    class ScheduleNextMaintenanceTests {

        private MaintenanceRecord record;

        @BeforeEach
        void init() {
            record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
        }

        @Test
        @DisplayName("sets the due date only")
        void setsDueDateOnly() {
            LocalDate due = LocalDate.now().plusMonths(6);

            record.scheduleNextMaintenance(due, null);

            assertThat(record.getNextMaintenanceDueDate()).isEqualTo(due);
            assertThat(record.getNextMaintenanceDueHours()).isNull();
        }

        @Test
        @DisplayName("sets the due hours only")
        void setsDueHoursOnly() {
            record.scheduleNextMaintenance(null, 1500.0);

            assertThat(record.getNextMaintenanceDueHours()).isEqualTo(1500.0);
            assertThat(record.getNextMaintenanceDueDate()).isNull();
        }

        @Test
        @DisplayName("rejects when both date and hours are null")
        void rejectsBothNull() {
            assertThatThrownBy(() -> record.scheduleNextMaintenance(null, null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dueDate or dueHours");
        }

        @Test
        @DisplayName("rejects non-positive due hours")
        void rejectsNonPositiveDueHours() {
            assertThatThrownBy(() -> record.scheduleNextMaintenance(null, 0.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Due hours");

            assertThatThrownBy(() -> record.scheduleNextMaintenance(null, -100.0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Due hours");
        }
    }

    
    
    

    @Nested
    @DisplayName("adjustExpectedDuration()")
    class AdjustExpectedDurationTests {

        @Test
        @DisplayName("updates the expected duration while PLANNED")
        void updatesWhilePlanned() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            record.adjustExpectedDuration(10.0);

            assertThat(record.getExpectedDurationHours()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("throws IllegalStateException once work has started")
        void throwsOnceStarted() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
            record.startWork();

            assertThatThrownBy(() -> record.adjustExpectedDuration(10.0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PLANNED");
        }

        @Test
        @DisplayName("rejects a null duration")
        void rejectsNullDuration() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            assertThatThrownBy(() -> record.adjustExpectedDuration(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("rejects a zero duration")
        void rejectsZeroDuration() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            assertThatThrownBy(() -> record.adjustExpectedDuration(0.0))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    
    
    

    @Nested
    @DisplayName("isOngoing()")
    class IsOngoingTests {

        @Test
        @DisplayName("returns true when PLANNED")
        void trueWhenPlanned() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);

            assertThat(record.isOngoing()).isTrue();
        }

        @Test
        @DisplayName("returns true when IN_PROGRESS")
        void trueWhenInProgress() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
            record.startWork();

            assertThat(record.isOngoing()).isTrue();
        }

        @Test
        @DisplayName("returns false when COMPLETED")
        void falseWhenCompleted() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
            record.startWork();
            record.markAsCompleted("Done", 5.0);

            assertThat(record.isOngoing()).isFalse();
        }

        @Test
        @DisplayName("returns false when CANCELED")
        void falseWhenCanceled() {
            MaintenanceRecord record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
            record.cancel("Reason");

            assertThat(record.isOngoing()).isFalse();
        }
    }

    
    
    

    @Nested
    @DisplayName("completeChecklistItem()")
    class CompleteChecklistItemTests {

        private MaintenanceRecord record;

        @BeforeEach
        void init() {
            record = new MaintenanceRecord(
                    aircraft, templateForA320, "Routine A-Check",
                    LocalDate.now(), null, MaintenanceComponent.AIRFRAME, null);
        }

        @Test
        @DisplayName("marks the item at the given index as done")
        void marksItemDone() {
            record.completeChecklistItem(1);

            assertThat(record.getChecklist().get(1).isCompleted()).isTrue();
            assertThat(record.getChecklist().get(0).isCompleted()).isFalse();
        }

        @Test
        @DisplayName("throws IndexOutOfBoundsException for an invalid index")
        void throwsForInvalidIndex() {
            assertThatThrownBy(() -> record.completeChecklistItem(99))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        @DisplayName("throws IllegalStateException if the record is COMPLETED")
        void throwsIfCompleted() {
            record.startWork();
            record.markAsCompleted("Done", 5.0);

            assertThatThrownBy(() -> record.completeChecklistItem(0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("COMPLETED");
        }

        @Test
        @DisplayName("throws IllegalStateException if the record is CANCELED")
        void throwsIfCanceled() {
            record.cancel("Reason");

            assertThatThrownBy(() -> record.completeChecklistItem(0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("CANCELED");
        }

        @Test
        @DisplayName("propagates the already-done guard from ChecklistItem")
        void propagatesAlreadyDoneGuard() {
            record.completeChecklistItem(0);

            assertThatThrownBy(() -> record.completeChecklistItem(0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already marked as completed");
        }

        @Test
        @DisplayName("getChecklist() returns an unmodifiable view")
        void getChecklistIsUnmodifiable() {
            assertThatThrownBy(() -> record.getChecklist().add(new ChecklistItem("Hack")))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}